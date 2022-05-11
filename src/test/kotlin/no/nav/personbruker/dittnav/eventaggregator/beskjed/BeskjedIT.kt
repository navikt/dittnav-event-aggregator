package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.createEventRecords
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.delayUntilCommittedOffset
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import org.amshove.kluent.`should be equal to`
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

class BeskjedIT {
    private val database = LocalPostgresDatabase.migratedDb()

    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)

    private val beskjedRepository = BeskjedRepository(database)
    private val beskjedPersistingService = BrukernotifikasjonPersistingService(beskjedRepository)
    private val eventProcessor = BeskjedEventService(beskjedPersistingService, metricsProbe)
    private val beskjedTopicPartition = TopicPartition("beskjed", 0)
    private val beskjedConsumerMock = MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(beskjedTopicPartition.topic()))
        it.rebalance(listOf(beskjedTopicPartition))
        it.updateBeginningOffsets(mapOf(beskjedTopicPartition to 0))
    }
    private val beskjedConsumer = Consumer(beskjedTopicPartition.topic(), beskjedConsumerMock, eventProcessor)

    @Test
    fun `Skal lese inn Beskjed-eventer og skrive de til databasen`() {
        val beskjeder = createEventRecords(10, beskjedTopicPartition, AvroBeskjedObjectMother::createBeskjed)
        beskjeder.forEach { beskjedConsumerMock.addRecord(it) }

        runBlocking {
            beskjedConsumer.startPolling()
            delayUntilCommittedOffset(beskjedConsumerMock, beskjedTopicPartition, beskjeder.size.toLong())
            beskjedConsumer.stopPolling()

            database.dbQuery {
                getAllBeskjed().size
            } `should be equal to` beskjeder.size
        }
    }
}