package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getBeskjedByEventId
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksTestData
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getInnboksByEventId
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsSession
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData
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
    private val varselInaktivertProducer = mockk<VarselInaktivertProducer>(relaxed = true)
    private val eventConsumer = PeriodicDoneEventWaitingTableProcessor(donePersistingService, varselInaktivertProducer, dbMetricsProbe)

    private val systembruker = "dummySystembruker"
    private val fodselsnummer = "12345"
    private val done1 = DoneTestData.done("3", systembruker, fodselsnummer)
    private val done2 = DoneTestData.done("4", systembruker, fodselsnummer)
    private val done3 = DoneTestData.done("5", systembruker, fodselsnummer)

    @AfterAll
    fun tearDown() {
        runBlocking {
            eventConsumer.stop()
        }
    }

    @Test
    fun `setter Beskjed-event inaktivt hvis Done-event med samme eventId tidligere er mottatt`() {
        val beskjedWithExistingDoneEvent = BeskjedTestData.beskjed(eventId = done1.eventId, fodselsnummer = fodselsnummer, systembruker = systembruker)
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
        val oppgaveWithExistingDoneEvent =
            OppgaveTestData.oppgave(eventId = done2.eventId, fodselsnummer = fodselsnummer, systembruker = systembruker)
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
        val eventConsumer = PeriodicDoneEventWaitingTableProcessor(donePersistingService, varselInaktivertProducer, dbMetricsProbe)
        val innboksWithExistingDone = InnboksTestData.innboks(eventId = done3.eventId, fodselsnummer = fodselsnummer)
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
        val doneEvent = DoneTestData.done(
            eventId = expectedEventId,
            systembruker = expectedSystembruker,
            fodselsnummer = expectedFodselsnr
        )
        val associatedBeskjed = BeskjedTestData.beskjed(
            eventId = expectedEventId,
            fodselsnummer = expectedFodselsnr,
            systembruker = expectedSystembruker
        )

        runBlocking {
            database.dbQuery { createDoneEvent(doneEvent) }
            database.dbQuery { createBeskjed(associatedBeskjed) }

            val elementsInDoneTableBeforeProcessing = database.dbQuery { getAllDoneEventWithLimit(100) }
            val expectedNumberOfEventsAfterProcessing = elementsInDoneTableBeforeProcessing.size - 1

            eventConsumer.processDoneEvents()

            val elementsInDoneTableAfterProcessing = database.dbQuery { getAllDoneEventWithLimit(100) }
            elementsInDoneTableAfterProcessing.size shouldBe expectedNumberOfEventsAfterProcessing
        }
    }

    @Test
    fun `feiler ikke hvis event med samme eventId som Done-event ikke er mottatt`() {
        shouldNotThrow<Exception> {
            runBlocking {
                database.dbQuery { createDoneEvent(DoneTestData.done("-1")) }
                eventConsumer.processDoneEvents()
            }
        }
    }

    @Test
    fun `skal telle og lage metrikk paa antall done-eventer vi ikke fant tilhorende oppgave for`() {
        val beskjed = BeskjedTestData.beskjed()
        val doneEvents = listOf(
            DoneTestData.done(beskjed.eventId, beskjed.systembruker, beskjed.fodselsnummer),
            DoneTestData.done("utenMatch1"),
            DoneTestData.done("utenMatch2")
        )

        val slot = slot<suspend DBMetricsSession.() -> Unit>()
        coEvery { dbMetricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }
        runBlocking {
            database.dbQuery { createBeskjed(beskjed) }
            database.dbQuery { doneEvents.forEach { createDoneEvent(it) } }
            eventConsumer.processDoneEvents()
        }

        coVerify(exactly = 2) { metricsSession.countCachedEventForProducer("dummyAppnavn") }
    }
}
