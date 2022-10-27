package no.nav.personbruker.dittnav.eventaggregator.archive

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData.beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.archive.getAllArchivedBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDtoTestData.createDoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.getAllDoknotifikasjonStatusBeskjed
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.getAllDoknotifikasjonStatusInnboks
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.getAllDoknotifikasjonStatusOppgave
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.upsertDoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksTestData.innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.archive.getAllArchivedInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData.oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.archive.getAllArchivedOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.Duration.ofMillis
import java.time.Duration.ofMinutes

internal class PeriodicVarselArchiverTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val repository = VarselArchivingRepository(database)
    private val probe: ArchiveMetricsProbe = mockk(relaxed = true)

    private val gammelBeskjed = beskjed(eventId = "b1", forstBehandlet = nowTruncatedToMillis().minusDays(11))
    private val nyBeskjed = beskjed(eventId = "b2", forstBehandlet = nowTruncatedToMillis().minusDays(9))
    private val gammelOppgave = oppgave(eventId = "o1", forstBehandlet = nowTruncatedToMillis().minusDays(11))
    private val nyOppgave = oppgave(eventId = "o2", forstBehandlet = nowTruncatedToMillis().minusDays(9))
    private val gammelInnboks = innboks(eventId = "i1", forstBehandlet = nowTruncatedToMillis().minusDays(11))
    private val nyInnboks = innboks(eventId = "i2", forstBehandlet = nowTruncatedToMillis().minusDays(9))

    private val eksternVarslingStatusGammelBeskjed = createDoknotifikasjonStatusDto(gammelBeskjed.eventId, status = "FERDIGSTILT", kanaler = listOf("SMS"))
    private val eksternVarslingStatusNyBeskjed = createDoknotifikasjonStatusDto(nyBeskjed.eventId, status = "FERDIGSTILT", kanaler = listOf("SMS"))
    private val eksternVarslingStatusGammelOppgave = createDoknotifikasjonStatusDto(gammelOppgave.eventId, status = "FERDIGSTILT", kanaler = listOf("EPOST"))
    private val eksternVarslingStatusNyOppgave = createDoknotifikasjonStatusDto(nyOppgave.eventId, status = "FERDIGSTILT", kanaler = listOf("EPOST"))
    private val eksternVarslingStatusGammelInnboks = createDoknotifikasjonStatusDto(gammelInnboks.eventId, status = "FERDIGSTILT", kanaler = listOf("SMS"))
    private val eksternVarslingStatusNyInnboks = createDoknotifikasjonStatusDto(nyInnboks.eventId, status = "FERDIGSTILT", kanaler = listOf("SMS"))

    @BeforeAll
    fun setup() {
        runBlocking {
            database.dbQuery {
                createBeskjed(gammelBeskjed)
                createBeskjed(nyBeskjed)
                createOppgave(gammelOppgave)
                createOppgave(nyOppgave)
                createInnboks(gammelInnboks)
                createInnboks(nyInnboks)

                upsertDoknotifikasjonStatus(eksternVarslingStatusGammelBeskjed, VarselType.BESKJED)
                upsertDoknotifikasjonStatus(eksternVarslingStatusNyBeskjed, VarselType.BESKJED)
                upsertDoknotifikasjonStatus(eksternVarslingStatusGammelOppgave, VarselType.OPPGAVE)
                upsertDoknotifikasjonStatus(eksternVarslingStatusNyOppgave, VarselType.OPPGAVE)
                upsertDoknotifikasjonStatus(eksternVarslingStatusGammelInnboks, VarselType.INNBOKS)
                upsertDoknotifikasjonStatus(eksternVarslingStatusNyInnboks, VarselType.INNBOKS)
            }

            val archiver = PeriodicVarselArchiver(
                varselArchivingRepository = repository,
                archiveMetricsProbe = probe,
                ageThresholdDays = 10,
                interval = ofMinutes(10)
            )
            archiver.start()
            delayUntilVarslerDeleted(3)
            archiver.stop()
        }
    }

    @Test
    fun `arkiverer alle gamle varsler`() = runBlocking {
        database.dbQuery { getAllArchivedBeskjed() }.size shouldBe 1
        database.dbQuery { getAllBeskjed() }.size shouldBe 1

        database.dbQuery { getAllArchivedOppgave() }.size shouldBe 1
        database.dbQuery { getAllOppgave() }.size shouldBe 1

        database.dbQuery { getAllArchivedInnboks() }.size shouldBe 1
        database.dbQuery { getAllInnboks() }.size shouldBe 1

        database.dbQuery { getAllDoknotifikasjonStatusBeskjed() }.size shouldBe 1
        database.dbQuery { getAllDoknotifikasjonStatusOppgave() }.size shouldBe 1
        database.dbQuery { getAllDoknotifikasjonStatusInnboks() }.size shouldBe 1
    }

    @Test
    fun `arkiverer beskjed-data`() {
        runBlocking {
            val archivedBeskjeder = database.dbQuery { getAllArchivedBeskjed() }
            archivedBeskjeder.size shouldBe 1
            archivedBeskjeder.first().apply {
                fodselsnummer shouldBe gammelBeskjed.fodselsnummer
                eventId shouldBe gammelBeskjed.eventId
                tekst shouldBe gammelBeskjed.tekst
                link shouldBe gammelBeskjed.link
                sikkerhetsnivaa shouldBe gammelBeskjed.sikkerhetsnivaa
                aktiv shouldBe gammelBeskjed.aktiv
                produsentApp shouldBe gammelBeskjed.appnavn
                eksternVarslingSendt shouldBe (eksternVarslingStatusGammelBeskjed.status == "FERDIGSTILT")
                eksternVarslingKanaler shouldBe eksternVarslingStatusGammelBeskjed.kanaler.first()
                forstBehandlet shouldBe gammelBeskjed.forstBehandlet
            }
        }
    }

    @Test
    fun `arkiverer oppgave-data`() {
        runBlocking {
            val archivedOppgaver = database.dbQuery { getAllArchivedOppgave() }
            archivedOppgaver.size shouldBe 1
            archivedOppgaver.first().apply {
                fodselsnummer shouldBe gammelOppgave.fodselsnummer
                eventId shouldBe gammelOppgave.eventId
                tekst shouldBe gammelOppgave.tekst
                link shouldBe gammelOppgave.link
                sikkerhetsnivaa shouldBe gammelOppgave.sikkerhetsnivaa
                aktiv shouldBe gammelOppgave.aktiv
                produsentApp shouldBe gammelOppgave.appnavn
                eksternVarslingSendt shouldBe (eksternVarslingStatusGammelOppgave.status == "FERDIGSTILT")
                eksternVarslingKanaler shouldBe eksternVarslingStatusGammelOppgave.kanaler.first()
                forstBehandlet shouldBe gammelOppgave.forstBehandlet
            }
        }
    }

    @Test
    fun `arkiverer innboks-data`() {
        runBlocking {
            val archivedInnbokser = database.dbQuery { getAllArchivedInnboks() }
            archivedInnbokser.size shouldBe 1
            archivedInnbokser.first().apply {
                fodselsnummer shouldBe gammelInnboks.fodselsnummer
                eventId shouldBe gammelInnboks.eventId
                tekst shouldBe gammelInnboks.tekst
                link shouldBe gammelInnboks.link
                sikkerhetsnivaa shouldBe gammelInnboks.sikkerhetsnivaa
                aktiv shouldBe gammelInnboks.aktiv
                produsentApp shouldBe gammelInnboks.appnavn
                eksternVarslingSendt shouldBe (eksternVarslingStatusGammelInnboks.status == "FERDIGSTILT")
                eksternVarslingKanaler shouldBe eksternVarslingStatusGammelInnboks.kanaler.first()
                forstBehandlet shouldBe gammelInnboks.forstBehandlet
            }
        }
    }

    @Test
    @Disabled //tester PeriodicJob, ikke PeriodicVarselArchiver
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

    private suspend fun delayUntilVarslerDeleted(remainingVarsler: Int = 0) {
        withTimeout(1000) {
            while (database.dbQuery { antallVarsler() } > remainingVarsler) {
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


