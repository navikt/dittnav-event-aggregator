package no.nav.personbruker.dittnav.eventaggregator.innboks

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.InnboksIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.createEventRecords
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.delayUntilCommittedOffset
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

class InnboksIT {
    private val database = LocalPostgresDatabase.migratedDb()

    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)

    private val innboksRepository = InnboksRepository(database)
    private val innboksPersistingService = BrukernotifikasjonPersistingService(innboksRepository)
    private val eventProcessor = InnboksEventService(innboksPersistingService, metricsProbe)
    private val innboksTopicPartition = TopicPartition("innboks", 0)
    private val innboksConsumerMock = MockConsumer<NokkelIntern, InnboksIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(innboksTopicPartition.topic()))
        it.rebalance(listOf(innboksTopicPartition))
        it.updateBeginningOffsets(mapOf(innboksTopicPartition to 0))
    }
    private val innboksConsumer = Consumer(innboksTopicPartition.topic(), innboksConsumerMock, eventProcessor)

    @Test
    fun `Skal lese inn Innboks-eventer og skrive de til databasen`() {
        val innboksEventer = createEventRecords(10, innboksTopicPartition, AvroInnboksObjectMother::createInnboks)
        innboksEventer.forEach { innboksConsumerMock.addRecord(it) }

        runBlocking {
            innboksConsumer.startPolling()
            delayUntilCommittedOffset(innboksConsumerMock, innboksTopicPartition, innboksEventer.size.toLong())
            innboksConsumer.stopPolling()

            database.dbQuery {
                getAllInnboks().size shouldBe innboksEventer.size
            }
        }
    }
}