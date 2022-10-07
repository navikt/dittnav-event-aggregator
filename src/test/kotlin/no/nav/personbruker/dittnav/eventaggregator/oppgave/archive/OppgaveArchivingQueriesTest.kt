package no.nav.personbruker.dittnav.eventaggregator.oppgave.archive

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.oppgave.*
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus.upsertDoknotifikasjonStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalDateTime.now

internal class OppgaveArchivingQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    @AfterEach
    fun resetDatabase() = runBlocking<Unit> {
        database.dbQuery {
            deleteAllDoknotifikasjonStatusOppgave()
            deleteAllOppgave()
            deleteAllOppgaveArchive()
        }
    }

    @Test
    fun `should not fetch oppgave where forstBehandlet is after threshold date`() = runBlocking {
        val threshold = nowTruncatedToMillis().minusDays(10)

        createOppgaveInDb(forstBehandlet = nowTruncatedToMillis().minusDays(5))

        val result = database.dbQuery {
            getOppgaveAsArchiveDtoOlderThan(threshold)
        }

        result.isEmpty() shouldBe true
    }

    @Test
    fun `should fetch oppgave where forstBehandlet is before threshold date`() = runBlocking {
        val threshold = nowTruncatedToMillis().minusDays(10)

        val oppgave = createOppgaveInDb(forstBehandlet = nowTruncatedToMillis().minusDays(15))

        val result = database.dbQuery {
            getOppgaveAsArchiveDtoOlderThan(threshold)
        }

        val archivedOppgave = result.first()

        archivedOppgave.fodselsnummer shouldBe oppgave.fodselsnummer
        archivedOppgave.eventId shouldBe oppgave.eventId
        archivedOppgave.tekst shouldBe oppgave.tekst
        archivedOppgave.link shouldBe oppgave.link
        archivedOppgave.sikkerhetsnivaa shouldBe oppgave.sikkerhetsnivaa
        archivedOppgave.aktiv shouldBe oppgave.aktiv
        archivedOppgave.produsentApp shouldBe oppgave.appnavn
        archivedOppgave.forstBehandlet shouldBe oppgave.forstBehandlet

        archivedOppgave.eksternVarslingSendt shouldBe false
        archivedOppgave.eksternVarslingKanaler shouldBe ""
    }

    @Test
    fun `should parse eksternvarsling info from doknotifikasjon_status_oppgave if exists`() = runBlocking {
        val threshold = nowTruncatedToMillis().minusDays(10)

        val kanaler = "SMS"
        val eksternVarselSendtStatus = FERDIGSTILT

        val oppgave = createOppgaveInDb(forstBehandlet = nowTruncatedToMillis().minusDays(15))

        createDoknotStatusInDb(oppgave.eventId, eksternVarselSendtStatus, kanaler)

        val result = database.dbQuery {
            getOppgaveAsArchiveDtoOlderThan(threshold)
        }

        val archived = result.first()

        archived.fodselsnummer shouldBe oppgave.fodselsnummer
        archived.eventId shouldBe oppgave.eventId
        archived.tekst shouldBe oppgave.tekst
        archived.link shouldBe oppgave.link
        archived.sikkerhetsnivaa shouldBe oppgave.sikkerhetsnivaa
        archived.aktiv shouldBe oppgave.aktiv
        archived.produsentApp shouldBe oppgave.appnavn
        archived.forstBehandlet shouldBe oppgave.forstBehandlet

        archived.eksternVarslingSendt shouldBe true
        archived.eksternVarslingKanaler shouldBe kanaler
    }

    @Test
    fun `should delete corresponding oppgave`() = runBlocking {
        val oppgave = createOppgaveInDb(now())

        database.dbQuery {
            deleteOppgaveWithEventIds(listOf(oppgave.eventId))
        }

        val remainingOppgave = database.dbQuery {
            getAllOppgave()
        }

        remainingOppgave.isEmpty() shouldBe true
    }

    @Test
    fun `should delete corresponding doknotifikasjon_status_oppgave`() = runBlocking {
        val oppgave = createOppgaveInDb(now())
        createDoknotStatusInDb(oppgave.eventId, FERDIGSTILT, "")

        database.dbQuery {
            deleteDoknotifikasjonStatusOppgaveWithEventIds(listOf(oppgave.eventId))
        }

        val remainingDoknotStatus = database.dbQuery {
            getAllDoknotifikasjonStatusOppgave()
        }

        remainingDoknotStatus.isEmpty() shouldBe true
    }

    suspend fun createOppgaveInDb(forstBehandlet: LocalDateTime): Oppgave {
        val oppgave = OppgaveObjectMother.giveMeOppgaveWithForstBehandlet(forstBehandlet)

        return database.dbQuery {
            createOppgave(oppgave).entityId.let {
                oppgave.copy(id = it)
            }
        }
    }

    private suspend fun createDoknotStatusInDb(eventId: String, status: DoknotifikasjonStatusEnum, kanaler: String) {
        val doknotStatusOppgave = createDoknotifikasjonStatusDto(
            eventId = eventId,
            status = status.name,
            kanaler = kanaler.split(",")
        )

        database.dbQuery {
            upsertDoknotifikasjonStatus(doknotStatusOppgave, VarselType.OPPGAVE)
        }
    }
}
