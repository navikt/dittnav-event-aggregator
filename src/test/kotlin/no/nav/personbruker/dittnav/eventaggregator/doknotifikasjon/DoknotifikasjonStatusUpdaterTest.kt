package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother.giveMeBeskjedWithEventIdAndAppnavn
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.createPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother.giveMeOppgaveWithEventIdAndAppnavn
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class DoknotifikasjonStatusUpdaterTest {

    private val beskjedRepository: BeskjedRepository = mockk()
    private val oppgaveRepository: OppgaveRepository = mockk()
    private val doknotStatusRepository: DoknotifikasjonStatusRepository = mockk()

    private val statusUpdater = DoknotifikasjonStatusUpdater(beskjedRepository, oppgaveRepository, doknotStatusRepository)

    private val status1 = createDoknotifikasjonStatus("status-1")
    private val status2 = createDoknotifikasjonStatus("status-2")
    private val status3 = createDoknotifikasjonStatus("status-3")

    private val statuses = listOf(status1, status2, status3)

    @AfterEach
    fun cleanUp() {
        clearMocks(beskjedRepository, oppgaveRepository, doknotStatusRepository)
    }

    @Test
    fun `should attempt to match statuses with existing beskjed events and update`() {
        val beskjed1 = giveMeBeskjedWithEventIdAndAppnavn(status1.getBestillingsId(), status1.getBestillerId())
        val beskjed2 = giveMeBeskjedWithEventIdAndAppnavn(status2.getBestillingsId(), status2.getBestillerId())

        coEvery {
            beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any())
        } returns listOf(beskjed1, beskjed2)

        val persistResult = createPersistActionResult(
            successful = listOf(status1),
            unchanged = listOf(status2)
        )

        coEvery {
            doknotStatusRepository.updateStatusesForBeskjed(listOf(status1, status2))
        } returns persistResult

        val result = runBlocking {
            statusUpdater.updateStatusForBeskjed(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unchangedStatuses shouldContain status2
        result.unmatchedStatuses shouldContain status3

        coVerify(exactly = 1) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForBeskjed(any()) }
        coVerify(exactly = 0) { oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any()) }
    }

    @Test
    fun `should not consider status to match beskjed if appnavn differs from bestillingsId`() {
        val beskjed1 = giveMeBeskjedWithEventIdAndAppnavn(status1.getBestillingsId(), status1.getBestillerId())
        val beskjed2 = giveMeBeskjedWithEventIdAndAppnavn(status2.getBestillingsId(), "other")

        coEvery {
            beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any())
        } returns listOf(beskjed1, beskjed2)

        val persistResult = createPersistActionResult(
            successful = listOf(status1),
            unchanged = emptyList()
        )

        coEvery {
            doknotStatusRepository.updateStatusesForBeskjed(listOf(status1))
        } returns persistResult

        val result = runBlocking {
            statusUpdater.updateStatusForBeskjed(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unchangedStatuses.isEmpty() shouldBe true
        result.unmatchedStatuses shouldContainAll listOf(status2, status3)

        coVerify(exactly = 1) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForBeskjed(any()) }
        coVerify(exactly = 0) { oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any()) }
    }


    @Test
    fun `should attempt to match statuses with existing oppgave events and update`() {
        val oppgave1 = giveMeOppgaveWithEventIdAndAppnavn(status1.getBestillingsId(), status1.getBestillerId())
        val oppgave2 = giveMeOppgaveWithEventIdAndAppnavn(status2.getBestillingsId(), status2.getBestillerId())

        coEvery {
            oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any())
        } returns listOf(oppgave1, oppgave2)

        val persistResult = createPersistActionResult(
            successful = listOf(status1),
            unchanged = listOf(status2)
        )

        coEvery {
            doknotStatusRepository.updateStatusesForOppgave(listOf(status1, status2))
        } returns persistResult

        val result = runBlocking {
            statusUpdater.updateStatusForOppgave(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unchangedStatuses shouldContain status2
        result.unmatchedStatuses shouldContain status3

        coVerify(exactly = 1) { oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForOppgave(any()) }
        coVerify(exactly = 0) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
    }

    @Test
    fun `should not consider status to match oppgave if appnavn differs from bestillingsId`() {
        val oppgave1 = giveMeOppgaveWithEventIdAndAppnavn(status1.getBestillingsId(), status1.getBestillerId())
        val oppgave2 = giveMeOppgaveWithEventIdAndAppnavn(status2.getBestillingsId(), "other")

        coEvery {
            oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any())
        } returns listOf(oppgave1, oppgave2)

        val persistResult = createPersistActionResult(
            successful = listOf(status1),
            unchanged = emptyList()
        )

        coEvery {
            doknotStatusRepository.updateStatusesForOppgave(listOf(status1))
        } returns persistResult

        val result = runBlocking {
            statusUpdater.updateStatusForOppgave(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unchangedStatuses.isEmpty() shouldBe true
        result.unmatchedStatuses shouldContainAll listOf(status2, status3)

        coVerify(exactly = 1) { oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForOppgave(any()) }
        coVerify(exactly = 0) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
    }

}
