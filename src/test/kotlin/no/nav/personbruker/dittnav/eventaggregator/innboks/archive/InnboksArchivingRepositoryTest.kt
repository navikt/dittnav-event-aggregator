package no.nav.personbruker.dittnav.eventaggregator.innboks.archive

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDtoTestData
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.deleteAllDoknotifikasjonStatusInnboks
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.getAllDoknotifikasjonStatusInnboks
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.upsertDoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksTestData
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class InnboksArchivingRepositoryTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val repository = InnboksArchivingRepository(database)

    @AfterEach
    fun resetDatabase() = runBlocking<Unit> {
        database.dbQuery {
            deleteAllDoknotifikasjonStatusInnboks()
            deleteAllInnboks()
            deleteAllInnboksArchive()
        }
    }

    @Test
    fun `should move innboks to archive`() = runBlocking {
        val innboks = createInnboksInDb()
        val doknotStatus = createDoknotStatusInDb(innboks.eventId, FERDIGSTILT, "EPOST")

        val toArchive = mergeToArchiveDTO(innboks, doknotStatus)

        repository.moveToInnboksArchive(listOf(toArchive))

        val remainingInnboks = database.dbQuery { getAllInnboks() }
        val remainingDoknotStatus = database.dbQuery { getAllDoknotifikasjonStatusInnboks() }

        remainingInnboks.isEmpty() shouldBe true
        remainingDoknotStatus.isEmpty() shouldBe true

        val result = database.dbQuery {
            getAllArchivedInnboks()
        }

        val archivedInnboks = result.first()

        archivedInnboks.fodselsnummer shouldBe innboks.fodselsnummer
        archivedInnboks.eventId shouldBe innboks.eventId
        archivedInnboks.tekst shouldBe innboks.tekst
        archivedInnboks.link shouldBe innboks.link
        archivedInnboks.sikkerhetsnivaa shouldBe innboks.sikkerhetsnivaa
        archivedInnboks.aktiv shouldBe innboks.aktiv
        archivedInnboks.produsentApp shouldBe innboks.appnavn
        archivedInnboks.eksternVarslingSendt shouldBe true
        archivedInnboks.eksternVarslingKanaler shouldBe "EPOST"
        archivedInnboks.forstBehandlet shouldBe innboks.forstBehandlet
    }

    fun mergeToArchiveDTO(innboks: Innboks, doknotStatus: DoknotifikasjonStatusDto) =
        BrukernotifikasjonArchiveDTO(
            fodselsnummer = innboks.fodselsnummer,
            eventId = innboks.eventId,
            tekst = innboks.tekst,
            link = innboks.link,
            sikkerhetsnivaa = innboks.sikkerhetsnivaa,
            aktiv = innboks.aktiv,
            produsentApp = innboks.appnavn,
            eksternVarslingSendt = doknotStatus.status == FERDIGSTILT.name,
            eksternVarslingKanaler = doknotStatus.kanaler.joinToString(", "),
            forstBehandlet = innboks.forstBehandlet
        )

    suspend fun createInnboksInDb(): Innboks {
        val innboks = InnboksTestData.innboks()

        return database.dbQuery {
            createInnboks(innboks).entityId.let {
                innboks.copy(id = it)
            }
        }
    }

    private suspend fun createDoknotStatusInDb(eventId: String, status: DoknotifikasjonStatusEnum, kanaler: String): DoknotifikasjonStatusDto {
        val doknotStatusInnboks = DoknotifikasjonStatusDtoTestData.createDoknotifikasjonStatusDto(
            eventId = eventId,
            status = status.name,
            kanaler = kanaler.split(",")
        )

        database.dbQuery {
            upsertDoknotifikasjonStatus(doknotStatusInnboks, VarselType.INNBOKS)

        }

        return doknotStatusInnboks
    }
}
