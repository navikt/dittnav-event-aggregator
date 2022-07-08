package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class DoknotifikasjonStatusQueryTest {

    private val database = LocalPostgresDatabase.migratedDb()

    private val beskjed: Beskjed
    private val oppgave: Oppgave
    private val innboks: Innboks

    private val bestillingsIdBeskjed: String
    private val bestillingsIdOppgave: String
    private val bestillingsIdInnboks: String

    init {
        beskjed = createBeskjedInDb()
        oppgave = createOppgaveInDb()
        innboks = createInnboksInDb()

        bestillingsIdBeskjed = beskjed.eventId
        bestillingsIdOppgave = oppgave.eventId
        bestillingsIdInnboks = innboks.eventId
    }

    @AfterEach
    fun cleanDatabase() {
        runBlocking {
            database.dbQuery {
                deleteAllDoknotifikasjonStatusBeskjed()
                deleteAllDoknotifikasjonStatusOppgave()
                deleteAllDoknotifikasjonStatusInnboks()
            }
        }
    }

    @Test
    fun `should create insert new status for beskjed when none exists for eventId`() = runBlocking {
        val statusUpdate = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdBeskjed)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForBeskjed(listOf(statusUpdate))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonBeskjed()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate.eventId
        allStatuses[0].status shouldBe statusUpdate.status
        allStatuses[0].melding shouldBe statusUpdate.melding
        allStatuses[0].distribusjonsId shouldBe statusUpdate.distribusjonsId
        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate
    }

    @Test
    fun `should update status for beskjed when one already exists for eventId`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdBeskjed)

        val statusUpdate2 = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(
            eventId = bestillingsIdBeskjed,
            status = "new status",
            melding = "new melding",
            distribusjonsId = 321,
            antallOppdateringer = 2
        )

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForBeskjed(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonBeskjed()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate2.eventId
        allStatuses[0].status shouldBe statusUpdate2.status
        allStatuses[0].melding shouldBe statusUpdate2.melding
        allStatuses[0].distribusjonsId shouldBe statusUpdate2.distribusjonsId
        allStatuses[0].antallOppdateringer shouldBe 2


        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getPersistedEntitites() shouldContain statusUpdate2
    }

    @Test
    fun `should get status for beskjed from database`() = runBlocking {
        val statusUpdate = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdBeskjed)

        database.dbQuery {
            upsertDoknotifikasjonStatusForBeskjed(listOf(statusUpdate))
        }

        val statuses = database.dbQuery {
            getDoknotifikasjonStatusesForBeskjed(listOf(statusUpdate.eventId))
        }

        statuses.size shouldBe 1

        statuses[0].eventId shouldBe statusUpdate.eventId
        statuses[0].status shouldBe statusUpdate.status
        statuses[0].melding shouldBe statusUpdate.melding
        statuses[0].distribusjonsId shouldBe statusUpdate.distribusjonsId
        statuses[0].antallOppdateringer shouldBe 1
    }

    @Test
    fun `should create insert new status for oppgave when none exists for eventId`() = runBlocking {
        val statusUpdate = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdOppgave)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForOppgave(listOf(statusUpdate))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonOppgave()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate.eventId
        allStatuses[0].status shouldBe statusUpdate.status
        allStatuses[0].melding shouldBe statusUpdate.melding
        allStatuses[0].distribusjonsId shouldBe statusUpdate.distribusjonsId
        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate
    }

    @Test
    fun `should update status for oppgave when one already exists for eventId`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdOppgave)

        val statusUpdate2 = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(
            eventId = bestillingsIdOppgave,
            status = "new status",
            melding = "new melding",
            distribusjonsId = 321,
            antallOppdateringer = 2
        )

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForOppgave(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonOppgave()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate2.eventId
        allStatuses[0].status shouldBe statusUpdate2.status
        allStatuses[0].melding shouldBe statusUpdate2.melding
        allStatuses[0].distribusjonsId shouldBe statusUpdate2.distribusjonsId
        allStatuses[0].antallOppdateringer shouldBe 2

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getPersistedEntitites() shouldContain statusUpdate2
    }

    @Test
    fun `should get status for oppgave from database`() = runBlocking {
        val statusUpdate = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdOppgave)

        database.dbQuery {
            upsertDoknotifikasjonStatusForOppgave(listOf(statusUpdate))
        }

        val statuses = database.dbQuery {
            getDoknotifikasjonStatusesForOppgave(listOf(statusUpdate.eventId))
        }

        statuses.size shouldBe 1

        statuses[0].eventId shouldBe statusUpdate.eventId
        statuses[0].status shouldBe statusUpdate.status
        statuses[0].melding shouldBe statusUpdate.melding
        statuses[0].distribusjonsId shouldBe statusUpdate.distribusjonsId
        statuses[0].antallOppdateringer shouldBe 1
    }

    @Test
    fun `should create insert new status for innboks when none exists for eventId`() = runBlocking {
        val statusUpdate = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdInnboks)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForInnboks(listOf(statusUpdate))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonInnboks()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate.eventId
        allStatuses[0].status shouldBe statusUpdate.status
        allStatuses[0].melding shouldBe statusUpdate.melding
        allStatuses[0].distribusjonsId shouldBe statusUpdate.distribusjonsId
        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate
    }

    @Test
    fun `should update status for innboks when one already exists for eventId`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdInnboks)

        val statusUpdate2 = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(
            eventId = bestillingsIdInnboks,
            status = "new status",
            melding = "new melding",
            distribusjonsId = 321,
            antallOppdateringer = 2
        )

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForInnboks(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonInnboks()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate2.eventId
        allStatuses[0].status shouldBe statusUpdate2.status
        allStatuses[0].melding shouldBe statusUpdate2.melding
        allStatuses[0].distribusjonsId shouldBe statusUpdate2.distribusjonsId
        allStatuses[0].antallOppdateringer shouldBe 2

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getPersistedEntitites() shouldContain statusUpdate2
    }

    @Test
    fun `should get status for innboks from database`() = runBlocking {
        val statusUpdate = DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto(bestillingsIdInnboks)

        database.dbQuery {
            upsertDoknotifikasjonStatusForInnboks(listOf(statusUpdate))
        }

        val statuses = database.dbQuery {
            getDoknotifikasjonStatusesForInnboks(listOf(statusUpdate.eventId))
        }

        statuses.size shouldBe 1

        statuses[0].eventId shouldBe statusUpdate.eventId
        statuses[0].status shouldBe statusUpdate.status
        statuses[0].melding shouldBe statusUpdate.melding
        statuses[0].distribusjonsId shouldBe statusUpdate.distribusjonsId
        statuses[0].antallOppdateringer shouldBe 1
    }

    private fun createBeskjedInDb(): Beskjed {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed()
        return runBlocking {
            database.dbQuery {
                createBeskjed(beskjed).entityId.let {
                    beskjed.copy(id = it)
                }
            }
        }
    }

    private fun createOppgaveInDb(): Oppgave {
        val oppgave = OppgaveObjectMother.giveMeAktivOppgave()
        return runBlocking {
            database.dbQuery {
                createOppgave(oppgave).entityId.let {
                    oppgave.copy(id = it)
                }
            }
        }
    }

    private fun createInnboksInDb(): Innboks {
        val innboks = InnboksObjectMother.giveMeAktivInnboks()
        return runBlocking {
            database.dbQuery {
                createInnboks(innboks).entityId.let {
                    innboks.copy(id = it)
                }
            }
        }
    }
}
