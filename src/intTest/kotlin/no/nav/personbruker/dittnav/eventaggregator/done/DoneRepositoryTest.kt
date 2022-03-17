package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getBeskjedByEventId
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getInnboksByEventId
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getOppgaveByEventId
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain same`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class DoneRepositoryTest {

    val database = LocalPostgresDatabase.migratedDb()

    val doneRepository = DoneRepository(database)

    val beskjedRepository = BeskjedRepository(database)
    val oppgaveRepository = OppgaveRepository(database)
    val innboksRepository = InnboksRepository(database)

    private val done1 = DoneObjectMother.giveMeDone("11", "12345")
    private val done2 = DoneObjectMother.giveMeDone("12", "12345")
    private val done3 = DoneObjectMother.giveMeDone("13", "12345")

    @AfterEach
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllDone()
                deleteAllBeskjed()
                deleteAllOppgave()
                deleteAllInnboks()
            }
        }
    }

    @Test
    fun `Should return correct result for successful persists when all events are persisted in a single batch`() {
        runBlocking {
            val toCreate = listOf(done1, done2, done3)
            val result = doneRepository.createInOneBatch(toCreate)
            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should return correct result for successful persists when each event is persisted individually`() {
        runBlocking {
            val toCreate = listOf(done1, done2, done3)
            val result = doneRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)
            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should persist done-events in batch and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(done1, done2, done3)
            val alreadyPersisted = listOf(done1, done3)
            val expected = toCreate - alreadyPersisted
            doneRepository.createInOneBatch(alreadyPersisted)
            val result = doneRepository.createInOneBatch(toCreate)
            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }

    @Test
    fun `Should persist each done-events individually and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(done1, done2, done3)
            val alreadyPersisted = listOf(done1, done3)
            val expected = toCreate - alreadyPersisted
            doneRepository.createInOneBatch(alreadyPersisted)
            val result = doneRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)
            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }

    @Test
    fun `should match done event to beskjed even when systembruker differ`() = runBlocking<Unit> {
        val otherSystembruker = "beskjedSystembruker"

        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed(done1.eventId, done1.fodselsnummer, otherSystembruker)

        beskjedRepository.createInOneBatch(listOf(beskjed))
        doneRepository.writeDoneEventsForBeskjedToCache(listOf(done1))

        val result = database.dbQuery {
            getBeskjedByEventId(done1.eventId)
        }

        result.aktiv `should be equal to` false
        result.systembruker `should not be equal to` done1.systembruker
    }

    @Test
    fun `should match done event to oppgave even when systembruker differ`() = runBlocking<Unit> {
        val otherSystembruker = "oppgaveSystembruker"

        val oppgave = OppgaveObjectMother.giveMeAktivOppgave(done1.eventId, done1.fodselsnummer, otherSystembruker)

        oppgaveRepository.createInOneBatch(listOf(oppgave))
        doneRepository.writeDoneEventsForOppgaveToCache(listOf(done1))

        val result = database.dbQuery {
            getOppgaveByEventId(done1.eventId)
        }

        result.aktiv `should be equal to` false
        result.systembruker `should not be equal to` done1.systembruker
    }

    @Test
    fun `should match done event to innboks even when systembruker differ`() = runBlocking<Unit> {
        val otherSystembruker = "innboksSystembruker"

        val innboks = InnboksObjectMother.giveMeAktivInnboks(done1.eventId, done1.fodselsnummer, otherSystembruker)

        innboksRepository.createInOneBatch(listOf(innboks))
        doneRepository.writeDoneEventsForInnboksToCache(listOf(done1))

        val result = database.dbQuery {
            getInnboksByEventId(done1.eventId)
        }

        result.aktiv `should be equal to` false
        result.systembruker `should not be equal to` done1.systembruker
    }
}
