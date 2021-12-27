package no.nav.personbruker.dittnav.eventaggregator.oppgave

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.delayUntilCommittedOffset
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.createEventRecords
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import org.amshove.kluent.`should be equal to`
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class OppgaveTest {
    private val database = LocalPostgresDatabase()

    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)

    private val oppgaveRepository = OppgaveRepository(database)
    private val oppgavePersistingService = BrukernotifikasjonPersistingService(oppgaveRepository)
    private val eventProcessor = OppgaveEventService(oppgavePersistingService, metricsProbe)
    private val oppgaveTopicPartition = TopicPartition("oppgave", 0)
    private val oppgaveConsumerMock = MockConsumer<NokkelIntern, OppgaveIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(oppgaveTopicPartition.topic()))
        it.rebalance(listOf(oppgaveTopicPartition))
        it.updateBeginningOffsets(mapOf(oppgaveTopicPartition to 0))
    }
    private val oppgaveConsumer = Consumer(oppgaveTopicPartition.topic(), oppgaveConsumerMock, eventProcessor)

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllOppgave()
            }
        }
    }

    @Test
    fun `Skal lese inn Oppgave-eventer og skrive de til databasen`() {
        val oppgaver = createEventRecords(10, oppgaveTopicPartition, AvroOppgaveObjectMother::createOppgave)
        oppgaver.forEach { oppgaveConsumerMock.addRecord(it) }

        runBlocking {
            oppgaveConsumer.startPolling()
            delayUntilCommittedOffset(oppgaveConsumerMock, oppgaveTopicPartition, oppgaver.size.toLong())
            oppgaveConsumer.stopPolling()

            database.dbQuery {
                getAllOppgave().size
            } `should be equal to` oppgaver.size
        }
    }
}