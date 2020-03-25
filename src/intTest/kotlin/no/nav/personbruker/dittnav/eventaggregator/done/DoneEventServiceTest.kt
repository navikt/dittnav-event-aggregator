package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.beskjed.*
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.*
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import no.nav.personbruker.dittnav.eventaggregator.oppgave.*
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.shouldBeEmpty
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class DoneEventServiceTest {

    private val database = H2Database()
    private val metricsReporter = StubMetricsReporter()
    private val producerNameScrubber = ProducerNameScrubber("")
    private val metricsProbe = EventMetricsProbe(metricsReporter, producerNameScrubber)
    private val doneRepository = DoneRepository(database)
    private val beskjedRepository = BeskjedRepository(database)
    private val innboksRepository = InnboksRepository(database)
    private val oppgaveRepository = OppgaveRepository(database)
    private val doneEventService = DoneEventService(doneRepository, beskjedRepository, innboksRepository, oppgaveRepository, metricsProbe)
    private val beskjed1 = BeskjedObjectMother.giveMeBeskjed("1", "12345")
    private val oppgave = OppgaveObjectMother.giveMeOppgave("2", "12345")
    private val innboks = InnboksObjectMother.giveMeInnboks("3", "12345")
    private val beskjed2 = BeskjedObjectMother.giveMeBeskjed("4", "12345")
    private val nokkel1 = createNokkel(1)
    private val nokkel2 = createNokkel(2)
    private val nokkel3 = createNokkel(3)
    private val nokkel4 = createNokkel(4)
    private val nokkel99 = createNokkel(99)

    init {
        runBlocking {
            database.dbQuery {
                createBeskjed(beskjed1)
                createBeskjed(beskjed2)
                createOppgave(oppgave)
                createInnboks(innboks)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllOppgave()
                deleteAllBeskjed()
                deleteAllInnboks()
                deleteAllDone()
            }
        }
    }

    @Test
    fun `Setter Beskjed-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord<Nokkel, Done>(Kafka.beskjedTopicName, 1, 1, nokkel1, AvroDoneObjectMother.createDone("1"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allBeskjed = database.dbQuery { getAllBeskjed() }
            val foundBeskjed = allBeskjed.first { it.eventId == "1" }
            foundBeskjed.aktiv.`should be false`()
        }
    }

    @Test
    fun `Setter Oppgave-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord<Nokkel, Done>(Kafka.oppgaveTopicName, 1, 1, nokkel2, AvroDoneObjectMother.createDone("2"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allOppgave = database.dbQuery { getAllOppgave() }
            val foundOppgave = allOppgave.first { it.eventId == "2" }
            foundOppgave.aktiv.`should be false`()
        }
    }

    @Test
    fun `Flag Innboks-event as inactive if Done event is received`() {
        val record = ConsumerRecord<Nokkel, Done>(Kafka.innboksTopicName, 1, 1, nokkel3, AvroDoneObjectMother.createDone("3"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allInnboks = database.dbQuery { getAllInnboks() }
            val foundInnboks = allInnboks.first { it.eventId == "3" }
            foundInnboks.aktiv.`should be false`()
        }
    }

    @Test
    fun `Should not cache Done event if event with matching eventId exists`() {
        val record = ConsumerRecord<Nokkel, Done>(Kafka.beskjedTopicName, 1, 1, nokkel4, AvroDoneObjectMother.createDone("4"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.shouldBeEmpty()
        }
    }

    @Test
    fun `Lagrer Done-event i cache hvis event med matchende eventId ikke finnes`() {
        val record = ConsumerRecord<Nokkel, Done>(Kafka.beskjedTopicName, 1, 1, nokkel99, AvroDoneObjectMother.createDone("99"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.size `should be equal to` 1
            allDone.first().eventId `should be equal to` "99"
        }
    }
}
