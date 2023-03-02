package no.nav.personbruker.dittnav.eventaggregator.expired

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjedByAktiv
import no.nav.personbruker.dittnav.eventaggregator.beskjed.toBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertKilde
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertKilde.Frist
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertProducer
import no.nav.personbruker.dittnav.eventaggregator.metrics.DB_EVENTS_EXPIRED
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgaveByAktiv
import no.nav.personbruker.dittnav.eventaggregator.oppgave.toOppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType.Inaktivert
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection

internal class PeriodicExpiredVarselProcessorTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val varselInaktivertProducer = mockk<VarselInaktivertProducer>(relaxed = true)
    private val metricsReporter = mockk<MetricsReporter>()

    private val expiredVarselRepository = ExpiredVarselRepository(database)
    private val expiredVarselProcessor =
        PeriodicExpiredVarselProcessor(
            expiredVarselRepository,
            varselInaktivertProducer,
            ExpiredMetricsProbe(metricsReporter)
        )

    private val pastDate = nowTruncatedToMillis().minusDays(7)
    private val futureDate = nowTruncatedToMillis().plusDays(7)

    private val activeOppgave = OppgaveTestData.oppgave(
        synligFremTil = futureDate,
        eventId = "o1",
        aktiv = true,
        fristUtløpt = null
    )
    private val expiredOppgave = OppgaveTestData.oppgave(
        synligFremTil = pastDate,
        eventId = "o2",
        aktiv = true,
        fristUtløpt = null
    )

    private val activeBeskjed = BeskjedTestData.beskjed(eventId = "b1", aktiv = true, synligFremTil = futureDate)
    private val expiredBeskjed = BeskjedTestData.beskjed(eventId = "b2", aktiv = true, synligFremTil = pastDate)

    @AfterEach
    fun cleanUp() = runBlocking<Unit> {
        database.dbQuery {
            deleteAllBeskjed()
            deleteAllOppgave()
        }
        clearMocks(varselInaktivertProducer)
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

        database.dbQuery { getAllBeskjedByFristUtløpt() }.apply {
            size shouldBe 1
            first().eventId shouldBe expiredBeskjed.eventId
        }

        verify(exactly = 1) { varselInaktivertProducer.varselInaktivert(expiredBeskjed.hendelse(Inaktivert), Frist) }
        coVerify(exactly = 1) {
            metricsReporter.registerDataPoint(
                DB_EVENTS_EXPIRED,
                mapOf("counter" to 1),
                mapOf("eventType" to VarselType.BESKJED.name, "producer" to expiredBeskjed.appnavn)
            )
        }
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

        database.dbQuery { getAllOppgaveByFristUtløpt() }.apply {
            size shouldBe 1
            first().eventId shouldBe expiredOppgave.eventId
        }

        verify(exactly = 1) { varselInaktivertProducer.varselInaktivert(expiredOppgave.hendelse(Inaktivert), Frist) }
        coVerify(exactly = 1) {
            metricsReporter.registerDataPoint(
                DB_EVENTS_EXPIRED,
                mapOf("counter" to 1),
                mapOf("eventType" to VarselType.OPPGAVE.name, "producer" to expiredOppgave.appnavn)
            )
        }

    }
}

private fun Beskjed.hendelse(type: HendelseType) = VarselHendelse(type, VarselType.BESKJED, eventId, namespace, appnavn)
private fun Oppgave.hendelse(type: HendelseType) = VarselHendelse(type, VarselType.OPPGAVE, eventId, namespace, appnavn)

fun Connection.getAllBeskjedByFristUtløpt(): List<Beskjed> =
    prepareStatement("""SELECT * FROM beskjed WHERE frist_utløpt = TRUE""")
        .use {
            it.executeQuery().list {
                toBeskjed()
            }
        }

fun Connection.getAllOppgaveByFristUtløpt(): List<Oppgave> =
    prepareStatement("""SELECT * FROM oppgave WHERE frist_utløpt = TRUE""")
        .use {
            it.executeQuery().list {
                toOppgave()
            }
        }

