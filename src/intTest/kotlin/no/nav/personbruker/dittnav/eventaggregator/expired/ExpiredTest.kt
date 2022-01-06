package no.nav.personbruker.dittnav.eventaggregator.expired

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.DoneIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjedByAktiv
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.eventaggregator.done.DoneEventService
import no.nav.personbruker.dittnav.eventaggregator.done.DonePersistingService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.deleteAllDone
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.*
import org.amshove.kluent.`should be equal to`
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ExpiredTest {

    private val database = LocalPostgresDatabase()

    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)

    private val beskjedRepository = BeskjedRepository(database)
    private val beskjedPersistingService = BrukernotifikasjonPersistingService(beskjedRepository)
    private val eventProcessor = BeskjedEventService(beskjedPersistingService, metricsProbe)
    private val beskjedPartition = TopicPartition("beskjed", 0)
    private val beskjedConsumerMock = MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(beskjedPartition.topic()))
        it.rebalance(listOf(beskjedPartition))
        it.updateBeginningOffsets(mapOf(beskjedPartition to 0))
    }
    private val beskjedConsumer = Consumer(beskjedPartition.topic(), beskjedConsumerMock, eventProcessor)

    private val oppgaveRepository = OppgaveRepository(database)
    private val oppgavePersistingService = BrukernotifikasjonPersistingService(oppgaveRepository)
    private val oppgaveEventProcessor = OppgaveEventService(oppgavePersistingService, metricsProbe)
    private val oppgavePartition = TopicPartition("oppgave", 0)
    private val oppgaveConsumerMock = MockConsumer<NokkelIntern, OppgaveIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(oppgavePartition.topic()))
        it.rebalance(listOf(oppgavePartition))
        it.updateBeginningOffsets(mapOf(oppgavePartition to 0))
    }
    private val oppgaveConsumer = Consumer(oppgavePartition.topic(), oppgaveConsumerMock, oppgaveEventProcessor)

    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)
    private val doneProcessor = DoneEventService(donePersistingService, metricsProbe)
    private var donePartition = TopicPartition("done", 0)
    private var doneInternConsumerMock = MockConsumer<NokkelIntern, DoneIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(donePartition.topic()))
        it.rebalance(listOf(donePartition))
        it.updateBeginningOffsets(mapOf(donePartition to 0))
    }
    private var doneConsumer = Consumer("done", doneInternConsumerMock, doneProcessor)

    private val doneInternProducerMock = MockProducer<NokkelIntern, DoneIntern>()
    private val doneInputProducerMock = MockProducer<NokkelInput, DoneInput>()
    private val doneEmitter = DoneEventEmitter(KafkaProducerWrapper("done", doneInputProducerMock), "test-ns", "test-app")
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
        runBlocking {
            database.dbQuery {
                deleteAllBeskjed()
                deleteAllOppgave()
                deleteAllDone()
            }
        }
        doneInputProducerMock.clear()
        doneInternProducerMock.clear()
        donePartition = TopicPartition("done", 0)
        doneInternConsumerMock = MockConsumer<NokkelIntern, DoneIntern>(OffsetResetStrategy.EARLIEST).also {
            it.subscribe(listOf(donePartition.topic()))
            it.rebalance(listOf(donePartition))
            it.updateBeginningOffsets(mapOf(donePartition to 0))
        }
        doneConsumer = Consumer("done", doneInternConsumerMock, doneProcessor)
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
            mapAndForwardDoneRecords(doneInputProducerMock, doneInternProducerMock)
            doneInternProducerMock.history().size `should be equal to` expiredBeskjeder.size

            loopbackRecords(doneInternProducerMock, doneInternConsumerMock)
            delayUntilCommittedOffset(doneInternConsumerMock, donePartition, expiredBeskjeder.size.toLong())

            database.dbQuery {
                getAllBeskjedByAktiv(true).size
            } `should be equal to` 0
            beskjedConsumer.stopPolling()
            doneConsumer.stopPolling()
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
            mapAndForwardDoneRecords(doneInputProducerMock, doneInternProducerMock)
            doneInternProducerMock.history().size `should be equal to` expiredOppgaver.size

            loopbackRecords(doneInternProducerMock, doneInternConsumerMock)
            delayUntilCommittedOffset(doneInternConsumerMock, donePartition, expiredOppgaver.size.toLong())

            database.dbQuery {
                getAllOppgaveByAktiv(true).size
            } `should be equal to` 0
            oppgaveConsumer.stopPolling()
            doneConsumer.stopPolling()
        }
    }

    private fun genererateBeskjeder(): List<ConsumerRecord<NokkelIntern, BeskjedIntern>> {
        val beskjed = AvroBeskjedObjectMother.createBeskjed(
            10101,
            "beskjed",
            synligFremTil = Instant.now().minus(30, ChronoUnit.DAYS)
        )

        return (0..9).map {
            ConsumerRecord(
                beskjedPartition.topic(),
                beskjedPartition.partition(),
                it.toLong(),
                NokkelIntern("ulid", it.toString(), it.toString(), "12345678910", "test-ns", "test-app", "dummysystembruker"),
                beskjed
            )
        }
    }

    private fun genererateOppgaver(): List<ConsumerRecord<NokkelIntern, OppgaveIntern>> {
        val oppgave = AvroOppgaveObjectMother.createOppgave(
            10101,
            "beskjed",
            synligFremTil = LocalDateTime.now().minusDays(30)
        )

        return (0..9).map {
            ConsumerRecord(
                oppgavePartition.topic(),
                oppgavePartition.partition(),
                it.toLong(),
                NokkelIntern("ulid", it.toString(), it.toString(), "12345678910", "test-ns", "test-app", "dummysystembruker"),
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

    private fun mapAndForwardDoneRecords(input: MockProducer<NokkelInput, DoneInput>, intern: MockProducer<NokkelIntern, DoneIntern>) {
        input.history().forEach { inputRecord ->
            val internValue = inputRecord.value().toIntern()
            val internKey = inputRecord.key().toIntern()
            intern.send(ProducerRecord("done", 0, internKey, internValue))
        }
        intern.flush()
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

private fun NokkelInput.toIntern(): NokkelIntern = NokkelIntern(
    "ulid",
    getEventId(),
    getGrupperingsId(),
    getFodselsnummer(),
    getNamespace(),
    getAppnavn(),
    "dummysystembruker"
)

private fun DoneInput.toIntern(): DoneIntern = DoneIntern(
    getTidspunkt()
)
