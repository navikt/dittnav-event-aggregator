package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getBeskjedByEventId
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getInnboksByEventId
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsSession
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getOppgaveByEventId
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class PeriodicDoneEventWaitingTableProcessorTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)
    private val dbMetricsProbe = mockk<DBMetricsProbe>(relaxed = true)
    private val metricsSession = mockk<DBMetricsSession>(relaxed = true)
    private val eventConsumer = PeriodicDoneEventWaitingTableProcessor(donePersistingService, dbMetricsProbe)

    private val systembruker = "dummySystembruker"
    private val fodselsnummer = "12345"
    private val done1 = DoneObjectMother.giveMeDone("3", systembruker, fodselsnummer)
    private val done2 = DoneObjectMother.giveMeDone("4", systembruker, fodselsnummer)
    private val done3 = DoneObjectMother.giveMeDone("5", systembruker, fodselsnummer)

    @AfterAll
    fun tearDown() {
        runBlocking {
            eventConsumer.stop()
        }
    }

    @Test
    fun `setter Beskjed-event inaktivt hvis Done-event med samme eventId tidligere er mottatt`() {
        val beskjedWithExistingDoneEvent = BeskjedObjectMother.giveMeAktivBeskjed(done1.eventId, fodselsnummer, systembruker)
        runBlocking {
            database.dbQuery { createDoneEvent(done1) }
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
            database.dbQuery { createDoneEvent(done2) }
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
            database.dbQuery { createDoneEvent(done3) }
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
            elementsInDoneTableAfterProcessing.size shouldBe expectedNumberOfEventsAfterProcessing
        }
    }

    @Test
    fun `feiler ikke hvis event med samme eventId som Done-event ikke er mottatt`() {
        shouldNotThrow<Exception> {
            runBlocking {
                database.dbQuery { createDoneEvents(listOf(DoneObjectMother.giveMeDone("-1"))) }
                eventConsumer.processDoneEvents()
            }
        }
    }

    @Test
    fun `skal telle og lage metrikk paa antall done-eventer vi ikke fant tilhorende oppgave for`() {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed()
        val doneEvents = listOf(
            DoneObjectMother.giveMeDone(beskjed.eventId, beskjed.systembruker, beskjed.fodselsnummer),
            DoneObjectMother.giveMeDone("utenMatch1"),
            DoneObjectMother.giveMeDone("utenMatch2"))

        val slot = slot<suspend DBMetricsSession.() -> Unit>()
        coEvery { dbMetricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }
        runBlocking {
            database.dbQuery { createBeskjed(beskjed) }
            database.dbQuery { createDoneEvents(doneEvents) }
            eventConsumer.processDoneEvents()
        }

        coVerify(exactly = 2) { metricsSession.countCachedEventForProducer("dummyAppnavn") }
    }
}
