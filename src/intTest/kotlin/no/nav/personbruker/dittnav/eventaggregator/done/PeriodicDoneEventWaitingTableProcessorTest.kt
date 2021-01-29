package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getBeskjedByEventId
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getInnboksByEventId
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameResolver
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getOppgaveByEventId
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class PeriodicDoneEventWaitingTableProcessorTest {

    private val database = H2Database()
    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)
    private val nameResolver = ProducerNameResolver(database)
    private val dbMetricsProbe = DBMetricsProbe(StubMetricsReporter(), ProducerNameScrubber(nameResolver))
    private val eventConsumer = PeriodicDoneEventWaitingTableProcessor(donePersistingService, dbMetricsProbe)

    private val systembruker = "dummySystembruker"
    private val fodselsnummer = "12345"
    private val beskjed1 = BeskjedObjectMother.giveMeAktivBeskjed("1", fodselsnummer, systembruker)
    private val oppgave1 = OppgaveObjectMother.giveMeAktivOppgave("2", fodselsnummer, systembruker)
    private val done1 = DoneObjectMother.giveMeDone("3", systembruker, fodselsnummer)
    private val done2 = DoneObjectMother.giveMeDone("4", systembruker, fodselsnummer)
    private val done3 = DoneObjectMother.giveMeDone("5", systembruker, fodselsnummer)

    init {
        runBlocking {
            database.dbQuery {
                createBeskjed(beskjed1)
                createOppgave(oppgave1)
                createDoneEvents(listOf(done1, done2, done3))
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            eventConsumer.stop()
            database.dbQuery {
                deleteAllBeskjed()
                deleteAllOppgave()
                deleteAllInnboks()
                deleteAllDone()
            }
        }
    }

    @Test
    fun `setter Beskjed-event inaktivt hvis Done-event med samme eventId tidligere er mottatt`() {
        val beskjedWithExistingDoneEvent = BeskjedObjectMother.giveMeAktivBeskjed(done1.eventId, fodselsnummer, systembruker)
        runBlocking {
            database.dbQuery { createBeskjed(beskjedWithExistingDoneEvent) }
            eventConsumer.processDoneEvents()
            val beskjed = database.dbQuery { getBeskjedByEventId(done1.eventId) }
            beskjed.aktiv.shouldBeFalse()
        }
    }

    @Test
    fun `setter Oppgave-event inaktivt hvis Done-event med samme eventId tidligere er mottatt`() {
        val oppgaveWithExistingDoneEvent = OppgaveObjectMother.giveMeAktivOppgave(done2.eventId, fodselsnummer, systembruker)
        runBlocking {
            database.dbQuery { createOppgave(oppgaveWithExistingDoneEvent) }
            eventConsumer.processDoneEvents()
            val oppgave = database.dbQuery { getOppgaveByEventId(done2.eventId) }
            oppgave.aktiv.shouldBeFalse()
        }
    }

    @Test
    fun `setter Innboks-event inaktivt hvis Done-event med samme eventId tidligere er mottatt`() {
        val eventConsumer = PeriodicDoneEventWaitingTableProcessor(donePersistingService, dbMetricsProbe)
        val innboksWithExistingDone = InnboksObjectMother.giveMeAktivInnboks(done3.eventId, fodselsnummer, systembruker)
        runBlocking {
            database.dbQuery { createInnboks(innboksWithExistingDone) }
            eventConsumer.processDoneEvents()
            val innboks = database.dbQuery { getInnboksByEventId(done3.eventId) }
            innboks.aktiv.shouldBeFalse()
        }
    }

    @Test
    fun `fjerner done-eventer fra ventetabellen hvis tilhorende event blir funnet og satt aktivt`() {
        val expectedEventId = "50"
        val expectedFodselsnr = "45678"
        val expectedSystembruker = "dummySystembruker"
        val doneEvent = DoneObjectMother.giveMeDone(expectedEventId, expectedSystembruker, expectedFodselsnr)
        val associatedBeskjed = BeskjedObjectMother.giveMeAktivBeskjed(expectedEventId, expectedFodselsnr, expectedSystembruker)

        runBlocking {
            database.dbQuery { createDoneEvents(listOf(doneEvent)) }
            database.dbQuery { createBeskjed(associatedBeskjed) }

            val elementsInDoneTableBeforeProcessing = database.dbQuery { getAllDoneEvent() }
            val expectedNumberOfEventsAfterProcessing = elementsInDoneTableBeforeProcessing.size - 1

            eventConsumer.processDoneEvents()

            val elementsInDoneTableAfterProcessing = database.dbQuery { getAllDoneEvent() }
            elementsInDoneTableAfterProcessing.size `should be equal to` expectedNumberOfEventsAfterProcessing
        }
    }

    @Test
    fun `feiler ikke hvis event med samme eventId som Done-event ikke er mottatt`() {
        invoking {
            runBlocking {
                database.dbQuery { createDoneEvents(listOf(DoneObjectMother.giveMeDone("-1"))) }
                eventConsumer.processDoneEvents()
            }
        } `should not throw` AnyException
    }
}
