package no.nav.personbruker.dittnav.eventaggregator.innboks.archive

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.innboks.*
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus.upsertDoknotifikasjonStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class InnboksArchivingQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    @AfterEach
    fun resetDatabase() = runBlocking<Unit> {
        database.dbQuery {
            deleteAllDoknotifikasjonStatusInnboks()
            deleteAllInnboks()
            deleteAllInnboksArchive()
        }
    }

    @Test
    fun `should not fetch innboks where forstBehandlet is after threshold date`() = runBlocking {
        val threshold = nowTruncatedToMillis().minusDays(10)

        createInnboksInDb(forstBehandlet = nowTruncatedToMillis().minusDays(5))

        val result = database.dbQuery {
            getInnboksAsArchiveDtoOlderThan(threshold)
        }

        result.isEmpty() shouldBe true
    }

    @Test
    fun `should fetch innboks where forstBehandlet is before threshold date`() = runBlocking {
        val threshold = nowTruncatedToMillis().minusDays(10)

        val innboks = createInnboksInDb(forstBehandlet = nowTruncatedToMillis().minusDays(15))

        val result = database.dbQuery {
            getInnboksAsArchiveDtoOlderThan(threshold)
        }

        val archivedInnboks = result.first()

        archivedInnboks.fodselsnummer shouldBe innboks.fodselsnummer
        archivedInnboks.eventId shouldBe innboks.eventId
        archivedInnboks.tekst shouldBe innboks.tekst
        archivedInnboks.link shouldBe innboks.link
        archivedInnboks.sikkerhetsnivaa shouldBe innboks.sikkerhetsnivaa
        archivedInnboks.aktiv shouldBe innboks.aktiv
        archivedInnboks.produsentApp shouldBe innboks.appnavn
        archivedInnboks.forstBehandlet shouldBe innboks.forstBehandlet

        archivedInnboks.eksternVarslingSendt shouldBe false
        archivedInnboks.eksternVarslingKanaler shouldBe ""
    }

    @Test
    fun `should parse eksternvarsling info from doknotifikasjon_status_innboks if exists`() = runBlocking {
        val threshold = nowTruncatedToMillis().minusDays(10)

        val kanaler = "SMS"
        val eksternVarselSendtStatus = FERDIGSTILT

        val innboks = createInnboksInDb(forstBehandlet = nowTruncatedToMillis().minusDays(15))

        createDoknotStatusInDb(innboks.eventId, eksternVarselSendtStatus, kanaler)

        val result = database.dbQuery {
            getInnboksAsArchiveDtoOlderThan(threshold)
        }

        val archived = result.first()

        archived.fodselsnummer shouldBe innboks.fodselsnummer
        archived.eventId shouldBe innboks.eventId
        archived.tekst shouldBe innboks.tekst
        archived.link shouldBe innboks.link
        archived.sikkerhetsnivaa shouldBe innboks.sikkerhetsnivaa
        archived.aktiv shouldBe innboks.aktiv
        archived.produsentApp shouldBe innboks.appnavn
        archived.forstBehandlet shouldBe innboks.forstBehandlet

        archived.eksternVarslingSendt shouldBe true
        archived.eksternVarslingKanaler shouldBe kanaler
    }

    @Test
    fun `should delete corresponding innboks`() = runBlocking {
        val innboks = createInnboksInDb(nowTruncatedToMillis())

        database.dbQuery {
            deleteInnboksWithEventIds(listOf(innboks.eventId))
        }

        val remainingInnboks = database.dbQuery {
            getAllInnboks()
        }

        remainingInnboks.isEmpty() shouldBe true
    }

    @Test
    fun `should delete corresponding doknotifikasjon_status_innboks`() = runBlocking {
        val innboks = createInnboksInDb(nowTruncatedToMillis())
        createDoknotStatusInDb(innboks.eventId, FERDIGSTILT, "")

        database.dbQuery {
            deleteDoknotifikasjonStatusInnboksWithEventIds(listOf(innboks.eventId))
        }

        val remainingDoknotStatus = database.dbQuery {
            getAllDoknotifikasjonStatusInnboks()
        }

        remainingDoknotStatus.isEmpty() shouldBe true
    }

    suspend fun createInnboksInDb(forstBehandlet: LocalDateTime): Innboks {
        val innboks = InnboksObjectMother.giveMeInnboksWithForstBehandlet(forstBehandlet)

        return database.dbQuery {
            createInnboks(innboks).entityId.let {
                innboks.copy(id = it)
            }
        }
    }

    private suspend fun createDoknotStatusInDb(eventId: String, status: DoknotifikasjonStatusEnum, kanaler: String) {
        val doknotStatusInnboks = createDoknotifikasjonStatusDto(
            eventId = eventId,
            status = status.name,
            kanaler = kanaler.split(",")
        )

        database.dbQuery {
            upsertDoknotifikasjonStatus(doknotStatusInnboks, VarselType.INNBOKS)

        }
    }
}
