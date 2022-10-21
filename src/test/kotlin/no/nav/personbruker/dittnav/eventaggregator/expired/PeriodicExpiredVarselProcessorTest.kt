package no.nav.personbruker.dittnav.eventaggregator.expired

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjedByAktiv
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgaveByAktiv
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PeriodicExpiredVarselProcessorTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val expiredVarselRepository = ExpiredVarselRepository(database)
    private val expiredVarselProcessor = PeriodicExpiredVarselProcessor(expiredVarselRepository)

    private val pastDate = nowTruncatedToMillis().minusDays(7)
    private val futureDate = nowTruncatedToMillis().plusDays(7)

    private val activeOppgave = OppgaveTestData.oppgave(eventId = "o1", aktiv = true, synligFremTil = futureDate)
    private val expiredOppgave = OppgaveTestData.oppgave(eventId = "o2", aktiv = true, synligFremTil = pastDate)

    private val activeBeskjed = BeskjedTestData.beskjed(eventId = "b1", aktiv = true, synligFremTil = futureDate)
    private val expiredBeskjed = BeskjedTestData.beskjed(eventId = "b2", aktiv = true, synligFremTil = pastDate)

    @AfterEach
    fun cleanUp() = runBlocking<Unit> {
        database.dbQuery {
            deleteAllBeskjed()
            deleteAllOppgave()
        }
    }

    @BeforeEach
    fun setup() = runBlocking<Unit> {
        database.dbQuery {
            createOppgave(activeOppgave)
            createOppgave(expiredOppgave)
            createBeskjed(activeBeskjed)
            createBeskjed(expiredBeskjed)
        }
    }

    @Test
    fun `Setter utgåtte beskjeder som inaktive`() = runBlocking {
        expiredVarselProcessor.updateExpiredBeskjed()

        val unchangedBeskjed = database.dbQuery {
            getAllBeskjedByAktiv(true).first()
        }

        val updatedBeskjed = database.dbQuery {
            getAllBeskjedByAktiv(false).first()
        }

        unchangedBeskjed.eventId shouldBe activeBeskjed.eventId
        unchangedBeskjed.sistOppdatert shouldBe activeBeskjed.sistOppdatert
        unchangedBeskjed.aktiv shouldBe true

        updatedBeskjed.eventId shouldBe expiredBeskjed.eventId
        updatedBeskjed.sistOppdatert shouldNotBe expiredBeskjed.sistOppdatert
        updatedBeskjed.aktiv shouldBe false
    }

    @Test
    fun `Setter utgåtte oppgaver som inaktive`() = runBlocking {
        expiredVarselProcessor.updateExpiredOppgave()

        val unchangedOppgave = database.dbQuery {
            getAllOppgaveByAktiv(true).first()
        }

        val updatedOppgave = database.dbQuery {
            getAllOppgaveByAktiv(false).first()
        }

        unchangedOppgave.eventId shouldBe activeOppgave.eventId
        unchangedOppgave.sistOppdatert shouldBe activeOppgave.sistOppdatert
        unchangedOppgave.aktiv shouldBe true

        updatedOppgave.eventId shouldBe expiredOppgave.eventId
        updatedOppgave.sistOppdatert shouldNotBe expiredOppgave.sistOppdatert
        updatedOppgave.aktiv shouldBe false
    }
}
