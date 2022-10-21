package no.nav.personbruker.dittnav.eventaggregator.oppgave.archive

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDtoTestData
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.deleteAllDoknotifikasjonStatusOppgave
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.getAllDoknotifikasjonStatusOppgave
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.upsertDoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class OppgaveArchivingRepositoryTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val repository = OppgaveArchivingRepository(database)

    @AfterEach
    fun resetDatabase() = runBlocking<Unit> {
        database.dbQuery {
            deleteAllDoknotifikasjonStatusOppgave()
            deleteAllOppgave()
            deleteAllOppgaveArchive()
        }
    }

    @Test
    fun `should move oppgave to archive`() = runBlocking {
        val oppgave = createOppgaveInDb()
        val doknotStatus = createDoknotStatusInDb(oppgave.eventId, FERDIGSTILT, "EPOST")

        val toArchive = mergeToArchiveDTO(oppgave, doknotStatus)

        repository.moveToOppgaveArchive(listOf(toArchive))

        val remainingOppgave = database.dbQuery { getAllOppgave() }
        val remainingDoknotStatus = database.dbQuery { getAllDoknotifikasjonStatusOppgave() }

        remainingOppgave.isEmpty() shouldBe true
        remainingDoknotStatus.isEmpty() shouldBe true

        val result = database.dbQuery {
            getAllArchivedOppgave()
        }

        val archivedOppgave = result.first()

        archivedOppgave.fodselsnummer shouldBe oppgave.fodselsnummer
        archivedOppgave.eventId shouldBe oppgave.eventId
        archivedOppgave.tekst shouldBe oppgave.tekst
        archivedOppgave.link shouldBe oppgave.link
        archivedOppgave.sikkerhetsnivaa shouldBe oppgave.sikkerhetsnivaa
        archivedOppgave.aktiv shouldBe oppgave.aktiv
        archivedOppgave.produsentApp shouldBe oppgave.appnavn
        archivedOppgave.eksternVarslingSendt shouldBe true
        archivedOppgave.eksternVarslingKanaler shouldBe "EPOST"
        archivedOppgave.forstBehandlet shouldBe oppgave.forstBehandlet
    }

    private fun mergeToArchiveDTO(oppgave: Oppgave, doknotStatus: DoknotifikasjonStatusDto) =
        BrukernotifikasjonArchiveDTO(
            fodselsnummer = oppgave.fodselsnummer,
            eventId = oppgave.eventId,
            tekst = oppgave.tekst,
            link = oppgave.link,
            sikkerhetsnivaa = oppgave.sikkerhetsnivaa,
            aktiv = oppgave.aktiv,
            produsentApp = oppgave.appnavn,
            eksternVarslingSendt = doknotStatus.status == FERDIGSTILT.name,
            eksternVarslingKanaler = doknotStatus.kanaler.joinToString(", "),
            forstBehandlet = oppgave.forstBehandlet
        )

    private suspend fun createOppgaveInDb(): Oppgave {
        val oppgave = OppgaveTestData.oppgave()

        return database.dbQuery {
            createOppgave(oppgave).entityId.let {
                oppgave.copy(id = it)
            }
        }
    }

    private suspend fun createDoknotStatusInDb(eventId: String, status: DoknotifikasjonStatusEnum, kanaler: String): DoknotifikasjonStatusDto {
        val doknotStatusOppgave = DoknotifikasjonStatusDtoTestData.createDoknotifikasjonStatusDto(
            eventId = eventId,
            status = status.name,
            kanaler = kanaler.split(",")
        )

        database.dbQuery {
            upsertDoknotifikasjonStatus(doknotStatusOppgave, VarselType.OPPGAVE)
        }

        return doknotStatusOppgave
    }
}
