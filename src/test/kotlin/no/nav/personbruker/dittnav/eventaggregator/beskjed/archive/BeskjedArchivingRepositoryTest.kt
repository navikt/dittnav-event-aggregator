package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDtoObjectMother
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.deleteAllDoknotifikasjonStatusBeskjed
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.getAllDoknotifikasjonStatusBeskjed
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.upsertDoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class BeskjedArchivingRepositoryTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val repository = BeskjedArchivingRepository(database)

    @AfterEach
    fun resetDatabase() = runBlocking<Unit> {
        database.dbQuery {
            deleteAllDoknotifikasjonStatusBeskjed()
            deleteAllBeskjed()
            deleteAllBeskjedArchive()
        }
    }

    @Test
    fun `should move beskjed to archive`() = runBlocking {
        val beskjed = createBeskjedInDb()
        val doknotStatus = createDoknotStatusInDb(beskjed.eventId, FERDIGSTILT, "EPOST")

        val toArchive = mergeToArchiveDTO(beskjed, doknotStatus)

        repository.moveToBeskjedArchive(listOf(toArchive))

        val remainingBeskjed = database.dbQuery { getAllBeskjed() }
        val remainingDoknotStatus = database.dbQuery { getAllDoknotifikasjonStatusBeskjed() }

        remainingBeskjed.isEmpty() shouldBe true
        remainingDoknotStatus.isEmpty() shouldBe true

        val result = database.dbQuery {
            getAllArchivedBeskjed()
        }

        val archivedBeskjed = result.first()

        archivedBeskjed.fodselsnummer shouldBe beskjed.fodselsnummer
        archivedBeskjed.eventId shouldBe beskjed.eventId
        archivedBeskjed.tekst shouldBe beskjed.tekst
        archivedBeskjed.link shouldBe beskjed.link
        archivedBeskjed.sikkerhetsnivaa shouldBe beskjed.sikkerhetsnivaa
        archivedBeskjed.aktiv shouldBe beskjed.aktiv
        archivedBeskjed.produsentApp shouldBe beskjed.appnavn
        archivedBeskjed.eksternVarslingSendt shouldBe true
        archivedBeskjed.eksternVarslingKanaler shouldBe "EPOST"
        archivedBeskjed.forstBehandlet shouldBe beskjed.forstBehandlet
    }

    fun mergeToArchiveDTO(beskjed: Beskjed, doknotStatus: DoknotifikasjonStatusDto) =
        BrukernotifikasjonArchiveDTO(
            fodselsnummer = beskjed.fodselsnummer,
            eventId = beskjed.eventId,
            tekst = beskjed.tekst,
            link = beskjed.link,
            sikkerhetsnivaa = beskjed.sikkerhetsnivaa,
            aktiv = beskjed.aktiv,
            produsentApp = beskjed.appnavn,
            eksternVarslingSendt = doknotStatus.status == FERDIGSTILT.name,
            eksternVarslingKanaler = doknotStatus.kanaler.joinToString(", "),
            forstBehandlet = beskjed.forstBehandlet
        )

    suspend fun createBeskjedInDb(): Beskjed {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed()

        return database.dbQuery {
            createBeskjed(beskjed).entityId.let {
                beskjed.copy(id = it)
            }
        }
    }

    private suspend fun createDoknotStatusInDb(eventId: String, status: DoknotifikasjonStatusEnum, kanaler: String): DoknotifikasjonStatusDto {
        val doknotStatusBeskjed = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(
            eventId = eventId,
            status = status.name,
            kanaler = kanaler.split(",")
        )

        database.dbQuery {
            upsertDoknotifikasjonStatus(doknotStatusBeskjed, VarselType.BESKJED)
        }

        return doknotStatusBeskjed
    }
}
