package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.StatusoppdateringIntern
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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class StatusoppdateringIT {
    private val database = LocalPostgresDatabase.migratedDb()

    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)

    private val statusoppdateringRepository = StatusoppdateringRepository(database)
    private val statusoppdateringPersistingService = BrukernotifikasjonPersistingService(statusoppdateringRepository)
    private val eventProcessor = StatusoppdateringEventService(statusoppdateringPersistingService, metricsProbe)
    private val statusoppdateringTopicPartition = TopicPartition("statusoppdatering", 0)
    private val statusoppdateringConsumerMock =
        MockConsumer<NokkelIntern, StatusoppdateringIntern>(OffsetResetStrategy.EARLIEST).also {
            it.subscribe(listOf(statusoppdateringTopicPartition.topic()))
            it.rebalance(listOf(statusoppdateringTopicPartition))
            it.updateBeginningOffsets(mapOf(statusoppdateringTopicPartition to 0))
        }
    private val statusoppdateringConsumer =
        Consumer(statusoppdateringTopicPartition.topic(), statusoppdateringConsumerMock, eventProcessor)

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllStatusoppdatering()
            }
        }
    }

    @Test
    fun `Skal lese inn Statusoppdatering-eventer og skrive de til databasen`() {
        val statusoppdateringer = createEventRecords(
            10,
            statusoppdateringTopicPartition,
            AvroStatusoppdateringObjectMother::createStatusoppdatering
        )
        statusoppdateringer.forEach { statusoppdateringConsumerMock.addRecord(it) }

        runBlocking {
            statusoppdateringConsumer.startPolling()
            delayUntilCommittedOffset(
                statusoppdateringConsumerMock,
                statusoppdateringTopicPartition,
                statusoppdateringer.size.toLong()
            )
            statusoppdateringConsumer.stopPolling()

            database.dbQuery {
                getAllStatusoppdatering().size
            } shouldBe statusoppdateringer.size
        }
    }
}