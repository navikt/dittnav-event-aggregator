package no.nav.personbruker.dittnav.eventaggregator.expired

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjedByAktiv
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.eventaggregator.done.DoneEventService
import no.nav.personbruker.dittnav.eventaggregator.done.DonePersistingService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.deleteAllDone
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameResolver
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.oppgave.*
import org.amshove.kluent.`should be equal to`
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ExpiredTest {

    private val database = H2Database()

    private val metricsReporter = StubMetricsReporter()
    private val nameResolver = ProducerNameResolver(database)
    private val nameScrubber = ProducerNameScrubber(nameResolver)
    private val metricsProbe = EventMetricsProbe(metricsReporter, nameScrubber)

    private val beskjedRepository = BeskjedRepository(database)
    private val beskjedPersistingService = BrukernotifikasjonPersistingService(beskjedRepository)
    private val eventProcessor = BeskjedEventService(beskjedPersistingService, metricsProbe)
    private val beskjedPartition = TopicPartition("beskjed", 0)
    private val beskjedConsumerMock = MockConsumer<Nokkel, Beskjed>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(beskjedPartition.topic()))
        it.rebalance(listOf(beskjedPartition))
        it.updateBeginningOffsets(mapOf(beskjedPartition to 0))
    }
    private val beskjedConsumer = Consumer(beskjedPartition.topic(), beskjedConsumerMock, eventProcessor)

    private val oppgaveRepository = OppgaveRepository(database)
    private val oppgavePersistingService = BrukernotifikasjonPersistingService(oppgaveRepository)
    private val oppgaveEventProcessor = OppgaveEventService(oppgavePersistingService, metricsProbe)
    private val oppgavePartition = TopicPartition("oppgave", 0)
    private val oppgaveConsumerMock = MockConsumer<Nokkel, Oppgave>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(oppgavePartition.topic()))
        it.rebalance(listOf(oppgavePartition))
        it.updateBeginningOffsets(mapOf(oppgavePartition to 0))
    }
    private val oppgaveConsumer = Consumer(oppgavePartition.topic(), oppgaveConsumerMock, oppgaveEventProcessor)

    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)
    private val doneProcessor = DoneEventService(donePersistingService, metricsProbe)
    private val donePartition = TopicPartition("done", 0)
    private var doneConsumerMock = MockConsumer<Nokkel, Done>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(donePartition.topic()))
        it.rebalance(listOf(donePartition))
        it.updateBeginningOffsets(mapOf(donePartition to 0))
    }
    private var doneConsumer = Consumer("done", doneConsumerMock, doneProcessor)

    private val doneProducerMock = MockProducer(
        false,
        {_:String, _:Nokkel -> ByteArray(0) }, //Dummy serializers
        {_:String, _:Done -> ByteArray(0) }
    )
    private val doneEmitter = DoneEventEmitter(KafkaProducerWrapper("done", doneProducerMock))
    private val expiredPersistingService = ExpiredPersistingService(database)
    private val periodicExpiredProcessor = PeriodicExpiredNotificationProcessor(expiredPersistingService, doneEmitter)

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllBeskjed()
                deleteAllOppgave()
                deleteAllDone()
            }
        }
    }

    @BeforeEach
    fun setUp() {
        doneProducerMock.clear()
        doneConsumerMock = MockConsumer<Nokkel, Done>(OffsetResetStrategy.EARLIEST).also {
            it.subscribe(listOf(donePartition.topic()))
            it.rebalance(listOf(donePartition))
            it.updateBeginningOffsets(mapOf(donePartition to 0))
        }
        doneConsumer = Consumer("done", doneConsumerMock, doneProcessor)
    }

    @Test
    fun `Utgåtte beskjeder blir satt til inaktive via done-event`() {
        beskjedConsumer.startPolling()
        doneConsumer.startPolling()
        val expiredBeskjeder = genererateBeskjeder()

        runBlocking {
            expiredBeskjeder.forEach { beskjedConsumerMock.addRecord(it) }
            delayUntilCommittedOffset(beskjedConsumerMock, beskjedPartition, expiredBeskjeder.size.toLong())

            database.dbQuery {
                getAllBeskjedByAktiv(true).size
            } `should be equal to` expiredBeskjeder.size

            periodicExpiredProcessor.sendDoneEventsForExpiredBeskjeder()
            doneProducerMock.history().size `should be equal to` expiredBeskjeder.size

            loopbackRecords(doneProducerMock, doneConsumerMock)
            delayUntilCommittedOffset(doneConsumerMock, donePartition, expiredBeskjeder.size.toLong())

            database.dbQuery {
                getAllBeskjedByAktiv(true).size
            } `should be equal to` 0
            beskjedConsumer.stopPolling()
            doneConsumer.startPolling()
        }
    }

    @Test
    fun `Utgåtte oppgaver blir satt til inaktive via done-event`() {
        oppgaveConsumer.startPolling()
        doneConsumer.startPolling()
        val expiredOppgaver = genererateOppgaver()

        runBlocking {
            expiredOppgaver.forEach { oppgaveConsumerMock.addRecord(it) }
            delayUntilCommittedOffset(oppgaveConsumerMock, oppgavePartition, expiredOppgaver.size.toLong())

            database.dbQuery {
                getAllOppgaveByAktiv(true).size
            } `should be equal to` expiredOppgaver.size

            periodicExpiredProcessor.sendDoneEventsForExpiredOppgaver()
            doneProducerMock.history().size `should be equal to` expiredOppgaver.size

            loopbackRecords(doneProducerMock, doneConsumerMock)
            delayUntilCommittedOffset(doneConsumerMock, donePartition, expiredOppgaver.size.toLong())

            database.dbQuery {
                getAllOppgaveByAktiv(true).size
            } `should be equal to` 0
            oppgaveConsumer.stopPolling()
            doneConsumer.stopPolling()
        }
    }

    private fun genererateBeskjeder(): List<ConsumerRecord<Nokkel, Beskjed>> {
        val beskjed = AvroBeskjedObjectMother.createBeskjed(
            10101,
            "12345678910",
            "beskjed",
            synligFremTil = LocalDateTime.now().minusDays(30)
        )

        return (0..9).map {
            ConsumerRecord(
                beskjedPartition.topic(),
                beskjedPartition.partition(),
                it.toLong(),
                Nokkel("dummySystembruker", it.toString()),
                beskjed
            )
        }
    }

    private fun genererateOppgaver(): List<ConsumerRecord<Nokkel, Oppgave>> {
        val oppgave = AvroOppgaveObjectMother.createOppgave(
            10101,
            "12345678910",
            "beskjed",
            synligFremTil = LocalDateTime.now().minusDays(30)
        )

        return (0..9).map {
            ConsumerRecord(
                oppgavePartition.topic(),
                oppgavePartition.partition(),
                it.toLong(),
                Nokkel("dummySystembruker", it.toString()),
                oppgave
            )
        }
    }

    private suspend fun <K, V> delayUntilCommittedOffset(
        consumer: MockConsumer<K, V>,
        partition: TopicPartition,
        offset: Long
    ) {
        withTimeout(1000) {
            while ((consumer.committed(setOf(partition))[partition]?.offset() ?: 0) < offset) {
                delay(10)
            }
        }
    }

    private fun <K, V> loopbackRecords(producer: MockProducer<K, V>, consumer: MockConsumer<K, V>) {
        var offset = 0L
        producer.history().forEach { producerRecord ->
            if (producerRecord.topic() in consumer.subscription()) {
                val partition =
                    TopicPartition(
                        producerRecord.topic(),
                        consumer.assignment().first { it.topic() == producerRecord.topic() }.partition()
                    )
                val consumerRecord = ConsumerRecord(
                    producerRecord.topic(),
                    partition.partition(),
                    offset++,
                    producerRecord.key(),
                    producerRecord.value()
                )
                consumer.addRecord(consumerRecord)
            }
        }
    }
}
