package no.nav.personbruker.dittnav.eventaggregator.archive

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData.beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.archive.deleteAllBeskjedArchive
import no.nav.personbruker.dittnav.eventaggregator.beskjed.archive.getAllArchivedBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksTestData.innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.archive.deleteAllInnboksArchive
import no.nav.personbruker.dittnav.eventaggregator.innboks.archive.getAllArchivedInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData.oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.archive.deleteAllOppgaveArchive
import no.nav.personbruker.dittnav.eventaggregator.oppgave.archive.getAllArchivedOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgave
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.Duration.ofMillis
import java.time.Duration.ofMinutes

internal class PeriodicVarselArchiverTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val repository = VarselArchivingRepository(database)
    private val probe: ArchiveMetricsProbe = mockk(relaxed = true)

    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
            database.dbQuery { deleteAllOppgave() }
            database.dbQuery { deleteAllInnboks() }
            database.dbQuery { deleteAllBeskjedArchive() }
            database.dbQuery { deleteAllOppgaveArchive() }
            database.dbQuery { deleteAllInnboksArchive() }
        }
    }

    @Test
    fun `skal flytte gamle varsler til arkivet`() = runBlocking {
        val gammelBeskjed = beskjed(eventId = "b1", forstBehandlet = nowTruncatedToMillis().minusDays(11))
        val nyBeskjed = beskjed(eventId = "b2", forstBehandlet = nowTruncatedToMillis().minusDays(9))
        val gammelOppgave = oppgave(eventId = "o1", forstBehandlet = nowTruncatedToMillis().minusDays(11))
        val nyOppgave = oppgave(eventId = "o2", forstBehandlet = nowTruncatedToMillis().minusDays(9))
        val gammelInnboks = innboks(eventId = "i1", forstBehandlet = nowTruncatedToMillis().minusDays(11))
        val nyInnboks = innboks(eventId = "i2", forstBehandlet = nowTruncatedToMillis().minusDays(9))

        database.dbQuery {
            createBeskjed(gammelBeskjed)
            createBeskjed(nyBeskjed)
            createOppgave(gammelOppgave)
            createOppgave(nyOppgave)
            createInnboks(gammelInnboks)
            createInnboks(nyInnboks)
        }

        val archiver = PeriodicVarselArchiver(
            varselArchivingRepository = repository,
            archiveMetricsProbe = probe,
            ageThresholdDays = 10,
            interval = ofMinutes(10)
        )
        archiver.start()
        withTimeout(1000) {
            while (database.dbQuery { antallVarsler() } > 3) {
                delay(100)
            }
        }
        archiver.stop()

        database.dbQuery { getAllArchivedBeskjed() }.size shouldBe 1
        database.dbQuery { getAllBeskjed() }.size shouldBe 1

        database.dbQuery { getAllArchivedOppgave() }.size shouldBe 1
        database.dbQuery { getAllOppgave() }.size shouldBe 1

        database.dbQuery { getAllArchivedInnboks() }.size shouldBe 1
        database.dbQuery { getAllInnboks() }.size shouldBe 1
    }

    @Test
    fun `starter på nytt etter et tidsintervall`() = runBlocking {
        val beskjed1 = beskjed(eventId = "1", forstBehandlet = nowTruncatedToMillis().minusDays(11))
        val beskjed2 = beskjed(eventId = "2", forstBehandlet = nowTruncatedToMillis().minusDays(12))

        database.dbQuery {
            createBeskjed(beskjed1)
        }

        val archiver = PeriodicVarselArchiver(
            varselArchivingRepository = repository,
            archiveMetricsProbe = probe,
            ageThresholdDays = 10,
            interval = ofMillis(100)
        )
        archiver.start()
        delayUntilVarslerDeleted()

        database.dbQuery {
            createBeskjed(beskjed2)
        }
        delayUntilVarslerDeleted()
        archiver.stop()

        database.dbQuery { getAllArchivedBeskjed() }.size shouldBe 2
        database.dbQuery { getAllBeskjed() }.size shouldBe 0
    }

    private suspend fun delayUntilVarslerDeleted() {
        withTimeout(1000) {
            while (database.dbQuery { antallVarsler() } > 0) {
                delay(100)
            }
        }
    }

    private fun Connection.antallVarsler(): Int =
        prepareStatement("""SELECT count(*) FROM brukernotifikasjon_view""")
            .use {
                it.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.getInt(1) else 0
                }
            }
}


