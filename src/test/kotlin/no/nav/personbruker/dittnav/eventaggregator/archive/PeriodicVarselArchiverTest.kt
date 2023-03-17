package no.nav.personbruker.dittnav.eventaggregator.archive

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData.beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowAtUtcTruncated
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternStatus.Sendt
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternVarslingStatusTestData.createEksternvarslingStatus
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksTestData.innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData.oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.Duration.ofMinutes

internal class PeriodicVarselArchiverTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val repository = VarselArchivingRepository(database)
    private val probe: ArchiveMetricsProbe = mockk(relaxed = true)

    private val gammelBeskjed =
        beskjed(eventId = "b1", forstBehandlet = nowAtUtcTruncated().minusDays(11), fristUtløpt = false)
    private val nyBeskjed = beskjed(eventId = "b2", forstBehandlet = nowAtUtcTruncated().minusDays(9))
    private val gammelOppgave = oppgave(
        forstBehandlet = nowAtUtcTruncated().minusDays(11),
        eventId = "o1",
        fristUtløpt = null
    )
    private val nyOppgave = oppgave(
        forstBehandlet = nowAtUtcTruncated().minusDays(9),
        eventId = "o2",
        fristUtløpt = null
    )
    private val gammelInnboks = innboks(eventId = "i1", forstBehandlet = nowAtUtcTruncated().minusDays(11))
    private val nyInnboks = innboks(eventId = "i2", forstBehandlet = nowAtUtcTruncated().minusDays(9))

    private val eksternVarslingStatusGammelBeskjed =
        createEksternvarslingStatus(gammelBeskjed.eventId, status = Sendt, kanal = "SMS")
    private val eksternVarslingStatusNyBeskjed =
        createEksternvarslingStatus(nyBeskjed.eventId, status = Sendt, kanal = "SMS")
    private val eksternVarslingStatusGammelOppgave =
        createEksternvarslingStatus(gammelOppgave.eventId, status = Sendt, kanal = "EPOST")
    private val eksternVarslingStatusNyOppgave =
        createEksternvarslingStatus(nyOppgave.eventId, status = Sendt, kanal = "EPOST")
    private val eksternVarslingStatusGammelInnboks =
        createEksternvarslingStatus(gammelInnboks.eventId, status = Sendt, kanal = "SMS")
    private val eksternVarslingStatusNyInnboks =
        createEksternvarslingStatus(nyInnboks.eventId, status = Sendt, kanal = "SMS")

    @BeforeAll
    fun setup() {
        runBlocking {
            database.dbQuery {
                createBeskjed(gammelBeskjed)
                createBeskjed(nyBeskjed)
                createBeskjed(beskjed())
                createOppgave(gammelOppgave)
                createOppgave(nyOppgave)
                createOppgave(oppgave(fristUtløpt = null))
                createInnboks(gammelInnboks)
                createInnboks(nyInnboks)
                createInnboks(innboks())

                upsertEksternVarslingStatus(eksternVarslingStatusGammelBeskjed, VarselType.BESKJED)
                upsertEksternVarslingStatus(eksternVarslingStatusNyBeskjed, VarselType.BESKJED)
                upsertEksternVarslingStatus(eksternVarslingStatusGammelOppgave, VarselType.OPPGAVE)
                upsertEksternVarslingStatus(eksternVarslingStatusNyOppgave, VarselType.OPPGAVE)
                upsertEksternVarslingStatus(eksternVarslingStatusGammelInnboks, VarselType.INNBOKS)
                upsertEksternVarslingStatus(eksternVarslingStatusNyInnboks, VarselType.INNBOKS)
            }

            val archiver = PeriodicVarselArchiver(
                varselArchivingRepository = repository,
                archiveMetricsProbe = probe,
                ageThresholdDays = 10,
                interval = ofMinutes(10)
            )
            archiver.start()
            delayUntilVarslerDeleted(6)
            archiver.stop()
        }
    }

    @Test
    fun `arkiverer alle gamle varsler`() = runBlocking {
        val arkiverteBeskjeder = database.dbQuery { getAllArchivedBeskjed() }
        arkiverteBeskjeder.size shouldBe 1
        arkiverteBeskjeder.first().apply {
            fristUtløpt shouldBe false
            eventId shouldBe gammelBeskjed.eventId
        }

        database.dbQuery { getAllBeskjed() }.size shouldBe 2

        val arkiverteOppgaver = database.dbQuery { getAllArchivedOppgave() }
        arkiverteOppgaver.size shouldBe 1
        arkiverteOppgaver.first().apply {
            fristUtløpt shouldBe null
            eventId shouldBe gammelOppgave.eventId
        }
        database.dbQuery { getAllOppgave() }.size shouldBe 2

        val arkiverteInnbokser = database.dbQuery { getAllArchivedInnboks() }
        arkiverteInnbokser.size shouldBe 1
        arkiverteInnbokser.first().apply {
            fristUtløpt shouldBe null
            eventId shouldBe gammelInnboks.eventId
        }
        database.dbQuery { getAllInnboks() }.size shouldBe 2

        database.dbQuery { countEksternVarslingStatusBeskjed() } shouldBe 1
        database.dbQuery { countEksternVarslingStatusOppgave() } shouldBe 1
        database.dbQuery { countEksternVarslingStatusInnboks() } shouldBe 1
    }

    @Test
    fun `arkiverer beskjed-data`() = runBlocking<Unit> {
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
            eksternVarslingSendt shouldBe eksternVarslingStatusGammelBeskjed.eksternVarslingSendt
            eksternVarslingKanaler shouldBe eksternVarslingStatusGammelBeskjed.kanaler.first()
            forstBehandlet shouldBe gammelBeskjed.forstBehandlet
        }
    }

    @Test
    fun `arkiverer oppgave-data`() = runBlocking<Unit> {
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
            eksternVarslingSendt shouldBe eksternVarslingStatusGammelBeskjed.eksternVarslingSendt
            eksternVarslingKanaler shouldBe eksternVarslingStatusGammelOppgave.kanaler.first()
            forstBehandlet shouldBe gammelOppgave.forstBehandlet
        }
    }

    @Test
    fun `arkiverer innboks-data`() = runBlocking<Unit> {
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
            eksternVarslingSendt shouldBe eksternVarslingStatusGammelBeskjed.eksternVarslingSendt
            eksternVarslingKanaler shouldBe eksternVarslingStatusGammelInnboks.kanaler.first()
            forstBehandlet shouldBe gammelInnboks.forstBehandlet
        }
    }

    private suspend fun delayUntilVarslerDeleted(remainingVarsler: Int = 0) {
        withTimeout(5000) {
            while (database.dbQuery { antallVarsler() } > remainingVarsler) {
                delay(100)
            }
        }
    }

    private fun Connection.antallVarsler(): Int =
        prepareStatement("""SELECT COUNT(*) FROM varsel_header_view""")
            .use {
                it.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.getInt(1) else 0
                }
            }
}


