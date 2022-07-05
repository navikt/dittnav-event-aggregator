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
        val statusUpdate = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdBeskjed)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForBeskjed(listOf(statusUpdate))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonBeskjed()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate.getBestillingsId()
        allStatuses[0].status shouldBe statusUpdate.getStatus()
        allStatuses[0].melding shouldBe statusUpdate.getMelding()
        allStatuses[0].distribusjonsId shouldBe statusUpdate.getDistribusjonId()
        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate
    }

    @Test
    fun `should update status for beskjed when one already exists for eventId`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdBeskjed)

        val statusUpdate2 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(
            bestillingsId = bestillingsIdBeskjed,
            status = "new status",
            melding = "new melding",
            distribusjonsId = 321
        )

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForBeskjed(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonBeskjed()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate2.getBestillingsId()
        allStatuses[0].status shouldBe statusUpdate2.getStatus()
        allStatuses[0].melding shouldBe statusUpdate2.getMelding()
        allStatuses[0].distribusjonsId shouldBe statusUpdate2.getDistribusjonId()
        allStatuses[0].antallOppdateringer shouldBe 2


        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getPersistedEntitites() shouldContain statusUpdate2
    }

    @Test
    fun `should not make any changes for identical status updates for beskjed`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdBeskjed)
        val statusUpdate2 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdBeskjed)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForBeskjed(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonBeskjed()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe false
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getUnalteredEntities() shouldContain statusUpdate2
    }

    @Test
    fun `should create insert new status for oppgave when none exists for eventId`() = runBlocking {
        val statusUpdate = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdOppgave)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForOppgave(listOf(statusUpdate))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonOppgave()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate.getBestillingsId()
        allStatuses[0].status shouldBe statusUpdate.getStatus()
        allStatuses[0].melding shouldBe statusUpdate.getMelding()
        allStatuses[0].distribusjonsId shouldBe statusUpdate.getDistribusjonId()
        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate
    }

    @Test
    fun `should update status for oppgave when one already exists for eventId`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdOppgave)

        val statusUpdate2 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(
            bestillingsId = bestillingsIdOppgave,
            status = "new status",
            melding = "new melding",
            distribusjonsId = 321
        )

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForOppgave(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonOppgave()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate2.getBestillingsId()
        allStatuses[0].status shouldBe statusUpdate2.getStatus()
        allStatuses[0].melding shouldBe statusUpdate2.getMelding()
        allStatuses[0].distribusjonsId shouldBe statusUpdate2.getDistribusjonId()
        allStatuses[0].antallOppdateringer shouldBe 2

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getPersistedEntitites() shouldContain statusUpdate2
    }

    @Test
    fun `should not make any changes for identical status updates for oppgave`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdOppgave)
        val statusUpdate2 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdOppgave)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForOppgave(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonOppgave()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe false
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getUnalteredEntities() shouldContain statusUpdate2
    }

    @Test
    fun `should create insert new status for innboks when none exists for eventId`() = runBlocking {
        val statusUpdate = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdInnboks)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForInnboks(listOf(statusUpdate))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonInnboks()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate.getBestillingsId()
        allStatuses[0].status shouldBe statusUpdate.getStatus()
        allStatuses[0].melding shouldBe statusUpdate.getMelding()
        allStatuses[0].distribusjonsId shouldBe statusUpdate.getDistribusjonId()
        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate
    }

    @Test
    fun `should update status for innboks when one already exists for eventId`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdInnboks)

        val statusUpdate2 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(
            bestillingsId = bestillingsIdInnboks,
            status = "new status",
            melding = "new melding",
            distribusjonsId = 321
        )

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForInnboks(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonInnboks()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].eventId shouldBe statusUpdate2.getBestillingsId()
        allStatuses[0].status shouldBe statusUpdate2.getStatus()
        allStatuses[0].melding shouldBe statusUpdate2.getMelding()
        allStatuses[0].distribusjonsId shouldBe statusUpdate2.getDistribusjonId()
        allStatuses[0].antallOppdateringer shouldBe 2

        persistResult.allEntitiesPersisted() shouldBe true
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getPersistedEntitites() shouldContain statusUpdate2
    }

    @Test
    fun `should not make any changes for identical status updates for innboks`() = runBlocking {
        val statusUpdate1 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdInnboks)
        val statusUpdate2 = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(bestillingsIdInnboks)

        val persistResult = database.dbQuery {
            upsertDoknotifikasjonStatusForInnboks(listOf(statusUpdate1, statusUpdate2))
        }

        val allStatuses = database.dbQuery {
            getAllDoknotifikasjonInnboks()
        }

        allStatuses.size shouldBe 1

        allStatuses[0].antallOppdateringer shouldBe 1

        persistResult.allEntitiesPersisted() shouldBe false
        persistResult.getPersistedEntitites() shouldContain statusUpdate1
        persistResult.getUnalteredEntities() shouldContain statusUpdate2
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
