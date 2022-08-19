package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.*
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalDateTime.now

internal class BeskjedArchivingQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    @AfterEach
    fun resetDatabase() = runBlocking<Unit> {
        database.dbQuery {
            deleteAllDoknotifikasjonStatusBeskjed()
            deleteAllBeskjed()
            deleteAllBeskjedArchive()
        }
    }

    @Test
    fun `should not fetch beskjed where forstBehandlet is after threshold date`() = runBlocking {
        val threshold = now().minusDays(10)

        createBeskjedInDb(forstBehandlet = now().minusDays(5))

        val result = database.dbQuery {
            getBeskjedAsArchiveDtoOlderThan(threshold)
        }

        result.isEmpty() shouldBe true
    }

    @Test
    fun `should fetch beskjed where forstBehandlet is before threshold date`() = runBlocking {
        val threshold = now().minusDays(10)

        val beskjed = createBeskjedInDb(forstBehandlet = now().minusDays(15))

        val result = database.dbQuery {
            getBeskjedAsArchiveDtoOlderThan(threshold)
        }

        val archivedBeskjed = result.first()

        archivedBeskjed.fodselsnummer shouldBe beskjed.fodselsnummer
        archivedBeskjed.eventId shouldBe beskjed.eventId
        archivedBeskjed.tekst shouldBe beskjed.tekst
        archivedBeskjed.link shouldBe beskjed.link
        archivedBeskjed.sikkerhetsnivaa shouldBe beskjed.sikkerhetsnivaa
        archivedBeskjed.aktiv shouldBe beskjed.aktiv
        archivedBeskjed.produsentApp shouldBe beskjed.appnavn
        archivedBeskjed.forstBehandlet shouldBe beskjed.forstBehandlet

        archivedBeskjed.eksternVarslingSendt shouldBe false
        archivedBeskjed.eksternVarslingKanaler shouldBe ""
    }

    @Test
    fun `should parse eksternvarsling info from doknotifikasjon_status_beskjed if exists`() = runBlocking {
        val threshold = now().minusDays(10)

        val kanaler = "SMS"
        val eksternVarselSendtStatus = FERDIGSTILT

        val beskjed = createBeskjedInDb(forstBehandlet = now().minusDays(15))

        createDoknotStatusInDb(beskjed.eventId, eksternVarselSendtStatus, kanaler)

        val result = database.dbQuery {
            getBeskjedAsArchiveDtoOlderThan(threshold)
        }

        val archived = result.first()

        archived.fodselsnummer shouldBe beskjed.fodselsnummer
        archived.eventId shouldBe beskjed.eventId
        archived.tekst shouldBe beskjed.tekst
        archived.link shouldBe beskjed.link
        archived.sikkerhetsnivaa shouldBe beskjed.sikkerhetsnivaa
        archived.aktiv shouldBe beskjed.aktiv
        archived.produsentApp shouldBe beskjed.appnavn
        archived.forstBehandlet shouldBe beskjed.forstBehandlet

        archived.eksternVarslingSendt shouldBe true
        archived.eksternVarslingKanaler shouldBe kanaler
    }

    @Test
    fun `should delete corresponding beskjed`() = runBlocking {
        val beskjed = createBeskjedInDb(now())

        database.dbQuery {
            deleteBeskjedWithEventIds(listOf(beskjed.eventId))
        }

        val remainingBeskjed = database.dbQuery {
            getAllBeskjed()
        }

        remainingBeskjed.isEmpty() shouldBe true
    }

    @Test
    fun `should delete corresponding doknotifikasjon_status_beskjed`() = runBlocking {
        val beskjed = createBeskjedInDb(now())
        createDoknotStatusInDb(beskjed.eventId, FERDIGSTILT, "")

        database.dbQuery {
            deleteDoknotifikasjonStatusBeskjedWithEventIds(listOf(beskjed.eventId))
        }

        val remainingDoknotStatus = database.dbQuery {
            getAllDoknotifikasjonStatusBeskjed()
        }

        remainingDoknotStatus.isEmpty() shouldBe true
    }

    suspend fun createBeskjedInDb(forstBehandlet: LocalDateTime): Beskjed {
        val beskjed = BeskjedObjectMother.giveMeBeskjedWithForstBehandlet(forstBehandlet)

        return database.dbQuery {
            createBeskjed(beskjed).entityId.let {
                beskjed.copy(id = it)
            }
        }
    }

    suspend fun createDoknotStatusInDb(eventId: String, status: DoknotifikasjonStatusEnum, kanaler: String) {
        val doknotStatusBeskjed = createDoknotifikasjonStatusDto(
            eventId = eventId,
            status = status.name,
            kanaler = kanaler.split(",")
        )

        database.dbQuery {
            upsertDoknotifikasjonStatusForBeskjed(listOf(doknotStatusBeskjed))
        }
    }
}