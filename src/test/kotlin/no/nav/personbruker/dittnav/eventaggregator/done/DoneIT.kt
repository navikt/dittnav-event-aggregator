package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.DoneIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjedByAktiv
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.createEventRecords
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.delayUntilCommittedOffset
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

class DoneIT {
    private val database = LocalPostgresDatabase.migratedDb()

    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)

    private val beskjedRepository = BeskjedRepository(database)
    private val beskjedPersistingService = BrukernotifikasjonPersistingService(beskjedRepository)
    private val beskjedEventProcessor = BeskjedEventService(beskjedPersistingService, metricsProbe)
    private val beskjedTopicPartition = TopicPartition("beskjed", 0)
    private val beskjedConsumerMock = MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(beskjedTopicPartition.topic()))
        it.rebalance(listOf(beskjedTopicPartition))
        it.updateBeginningOffsets(mapOf(beskjedTopicPartition to 0))
    }
    private val beskjedConsumer = Consumer(beskjedTopicPartition.topic(), beskjedConsumerMock, beskjedEventProcessor)

    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)
    private val doneEventProcessor = DoneEventService(donePersistingService, metricsProbe)
    private val doneTopicPartition = TopicPartition("done", 0)
    private val doneConsumerMock = MockConsumer<NokkelIntern, DoneIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(doneTopicPartition.topic()))
        it.rebalance(listOf(doneTopicPartition))
        it.updateBeginningOffsets(mapOf(doneTopicPartition to 0))
    }
    private val doneConsumer = Consumer(doneTopicPartition.topic(), doneConsumerMock, doneEventProcessor)

    @Test
    fun `Skal lese done-eventer og sette relaterte eventer til inaktive`() {
        val beskjeder = createEventRecords(10, beskjedTopicPartition, AvroBeskjedObjectMother::createBeskjed)
        beskjeder.forEach { beskjedConsumerMock.addRecord(it) }

        runBlocking {
            beskjedConsumer.startPolling()
            delayUntilCommittedOffset(beskjedConsumerMock, beskjedTopicPartition, beskjeder.size.toLong())
            beskjedConsumer.stopPolling()

            database.dbQuery {
                getAllBeskjedByAktiv(true).size
            } shouldBe beskjeder.size
        }

        beskjeder.forEach {
            val doneRecord = ConsumerRecord(
                doneTopicPartition.topic(),
                doneTopicPartition.partition(),
                it.offset(),
                it.key(),
                AvroDoneObjectMother.createDone()
            )
            doneConsumerMock.addRecord(doneRecord)
        }

        runBlocking {
            doneConsumer.startPolling()
            delayUntilCommittedOffset(doneConsumerMock, doneTopicPartition, beskjeder.size.toLong())
            doneConsumer.stopPolling()

            database.dbQuery {
                getAllBeskjedByAktiv(true).size
            } shouldBe 0
        }
    }
}