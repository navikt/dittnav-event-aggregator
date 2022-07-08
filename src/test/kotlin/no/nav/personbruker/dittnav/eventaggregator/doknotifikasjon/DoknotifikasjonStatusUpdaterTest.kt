package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother.giveMeBeskjedWithEventIdAndAppnavn
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.createPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDtoObjectMother.createDoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother.giveMeInnboksWithEventIdAndAppnavn
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother.giveMeOppgaveWithEventIdAndAppnavn
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class DoknotifikasjonStatusUpdaterTest {

    private val beskjedRepository: BeskjedRepository = mockk()
    private val oppgaveRepository: OppgaveRepository = mockk()
    private val innboksRepository: InnboksRepository = mockk()
    private val doknotStatusRepository: DoknotifikasjonStatusRepository = mockk()

    private val statusUpdater = DoknotifikasjonStatusUpdater(beskjedRepository, oppgaveRepository, innboksRepository, doknotStatusRepository)

    private val status1 = createDoknotifikasjonStatusDto("status-1")
    private val status2 = createDoknotifikasjonStatusDto("status-2")
    private val status3 = createDoknotifikasjonStatusDto("status-3")

    private val statuses = listOf(status1, status2, status3)

    @AfterEach
    fun cleanUp() {
        clearMocks(beskjedRepository, oppgaveRepository, innboksRepository, doknotStatusRepository)
    }

    @Test
    fun `should attempt to match statuses with existing beskjed events and update`() {
        val beskjed1 = giveMeBeskjedWithEventIdAndAppnavn(status1.eventId, status1.bestillerAppnavn)
        val beskjed2 = giveMeBeskjedWithEventIdAndAppnavn(status2.eventId, status2.bestillerAppnavn)

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

        coEvery {
            doknotStatusRepository.getStatusesForBeskjed(any())
        } returns emptyList()

        val result = runBlocking {
            statusUpdater.updateStatusForBeskjed(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unmatchedStatuses shouldContain status3

        coVerify(exactly = 1) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForBeskjed(any()) }
        coVerify(exactly = 0) { oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any()) }
    }

    @Test
    fun `should not consider status to match beskjed if appnavn differs from bestillingsId`() {
        val beskjed1 = giveMeBeskjedWithEventIdAndAppnavn(status1.eventId, status1.bestillerAppnavn)
        val beskjed2 = giveMeBeskjedWithEventIdAndAppnavn(status2.eventId, "other")

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

        coEvery {
            doknotStatusRepository.getStatusesForBeskjed(any())
        } returns emptyList()

        val result = runBlocking {
            statusUpdater.updateStatusForBeskjed(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unmatchedStatuses shouldContainAll listOf(status2, status3)

        coVerify(exactly = 1) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForBeskjed(any()) }
        coVerify(exactly = 0) { oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any()) }
    }

    @Test
    fun `should update if status already exists for beskjed`() {

        val newStatus1 = createDoknotifikasjonStatusDto("beskjed",kanaler = listOf("SMS"))

        val newStatus2 = createDoknotifikasjonStatusDto(
            newStatus1.eventId,
            kanaler = listOf("SMS"),
            status = "newestStatus",
            melding = "newestMelding"
        )

        val beskjed = giveMeBeskjedWithEventIdAndAppnavn(newStatus1.eventId, newStatus1.bestillerAppnavn)

        val existingStatus = createDoknotifikasjonStatusDto(
            eventId = newStatus1.eventId,
            antallOppdateringer = 3,
            kanaler = listOf("EPOST")
        )

        coEvery {
            beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any())
        } returns listOf(beskjed)

        val persistResult = createPersistActionResult(
            successful = listOf(newStatus2),
            unchanged = emptyList()
        )

        val persistedSlot = slot<List<DoknotifikasjonStatusDto>>()

        coEvery {
            doknotStatusRepository.getStatusesForBeskjed(listOf(newStatus1.eventId))
        } returns listOf(existingStatus)

        coEvery {
            doknotStatusRepository.updateStatusesForBeskjed(capture(persistedSlot))
        } returns persistResult

        runBlocking {
            statusUpdater.updateStatusForBeskjed(listOf(newStatus1, newStatus2))
        }

        val persistedStatus = persistedSlot.captured[0]

        persistedStatus.status shouldBe newStatus2.status
        persistedStatus.melding shouldBe newStatus2.melding
        persistedStatus.kanaler.size shouldBe 2
        persistedStatus.kanaler shouldContainAll listOf("SMS", "EPOST")
        persistedStatus.antallOppdateringer shouldBe 5
    }

    @Test
    fun `should attempt to match statuses with existing oppgave events and update`() {
        val oppgave1 = giveMeOppgaveWithEventIdAndAppnavn(status1.eventId, status1.bestillerAppnavn)
        val oppgave2 = giveMeOppgaveWithEventIdAndAppnavn(status2.eventId, status2.bestillerAppnavn)

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

        coEvery {
            doknotStatusRepository.getStatusesForOppgave(any())
        } returns emptyList()

        val result = runBlocking {
            statusUpdater.updateStatusForOppgave(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unmatchedStatuses shouldContain status3

        coVerify(exactly = 1) { oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForOppgave(any()) }
        coVerify(exactly = 0) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
    }

    @Test
    fun `should not consider status to match oppgave if appnavn differs from bestillingsId`() {
        val oppgave1 = giveMeOppgaveWithEventIdAndAppnavn(status1.eventId, status1.bestillerAppnavn)
        val oppgave2 = giveMeOppgaveWithEventIdAndAppnavn(status2.eventId, "other")

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

        coEvery {
            doknotStatusRepository.getStatusesForOppgave(any())
        } returns emptyList()

        val result = runBlocking {
            statusUpdater.updateStatusForOppgave(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unmatchedStatuses shouldContainAll listOf(status2, status3)

        coVerify(exactly = 1) { oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForOppgave(any()) }
        coVerify(exactly = 0) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
    }

    @Test
    fun `should update if status already exists for oppgave`() {

        val newStatus1 = createDoknotifikasjonStatusDto("oppgave",kanaler = listOf("SMS"))

        val newStatus2 = createDoknotifikasjonStatusDto(
            newStatus1.eventId,
            kanaler = listOf("SMS"),
            status = "newestStatus",
            melding = "newestMelding"
        )

        val oppgave = giveMeOppgaveWithEventIdAndAppnavn(newStatus1.eventId, newStatus1.bestillerAppnavn)

        val existingStatus = createDoknotifikasjonStatusDto(
            eventId = newStatus1.eventId,
            antallOppdateringer = 3,
            kanaler = listOf("EPOST")
        )

        coEvery {
            oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(any())
        } returns listOf(oppgave)

        val persistResult = createPersistActionResult(
            successful = listOf(newStatus2),
            unchanged = emptyList()
        )

        val persistedSlot = slot<List<DoknotifikasjonStatusDto>>()

        coEvery {
            doknotStatusRepository.getStatusesForOppgave(listOf(newStatus1.eventId))
        } returns listOf(existingStatus)

        coEvery {
            doknotStatusRepository.updateStatusesForOppgave(capture(persistedSlot))
        } returns persistResult

        runBlocking {
            statusUpdater.updateStatusForOppgave(listOf(newStatus1, newStatus2))
        }

        val persistedStatus = persistedSlot.captured[0]

        persistedStatus.status shouldBe newStatus2.status
        persistedStatus.melding shouldBe newStatus2.melding
        persistedStatus.kanaler.size shouldBe 2
        persistedStatus.kanaler shouldContainAll listOf("SMS", "EPOST")
        persistedStatus.antallOppdateringer shouldBe 5
    }

    @Test
    fun `should attempt to match statuses with existing innboks events and update`() {
        val innboks1 = giveMeInnboksWithEventIdAndAppnavn(status1.eventId, status1.bestillerAppnavn)
        val innboks2 = giveMeInnboksWithEventIdAndAppnavn(status2.eventId, status2.bestillerAppnavn)

        coEvery {
            innboksRepository.getInnboksWithEksternVarslingForEventIds(any())
        } returns listOf(innboks1, innboks2)

        val persistResult = createPersistActionResult(
            successful = listOf(status1),
            unchanged = listOf(status2)
        )

        coEvery {
            doknotStatusRepository.updateStatusesForInnboks(listOf(status1, status2))
        } returns persistResult

        coEvery {
            doknotStatusRepository.getStatusesForInnboks(any())
        } returns emptyList()

        val result = runBlocking {
            statusUpdater.updateStatusForInnboks(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unmatchedStatuses shouldContain status3

        coVerify(exactly = 1) { innboksRepository.getInnboksWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForInnboks(any()) }
        coVerify(exactly = 0) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
    }

    @Test
    fun `should not consider status to match innboks if appnavn differs from bestillingsId`() {
        val innboks1 = giveMeInnboksWithEventIdAndAppnavn(status1.eventId, status1.bestillerAppnavn)
        val innboks2 = giveMeInnboksWithEventIdAndAppnavn(status2.eventId, "other")

        coEvery {
            innboksRepository.getInnboksWithEksternVarslingForEventIds(any())
        } returns listOf(innboks1, innboks2)

        val persistResult = createPersistActionResult(
            successful = listOf(status1),
            unchanged = emptyList()
        )

        coEvery {
            doknotStatusRepository.updateStatusesForInnboks(listOf(status1))
        } returns persistResult

        coEvery {
            doknotStatusRepository.getStatusesForInnboks(any())
        } returns emptyList()

        val result = runBlocking {
            statusUpdater.updateStatusForInnboks(statuses)
        }

        result.updatedStatuses shouldContain status1
        result.unmatchedStatuses shouldContainAll listOf(status2, status3)

        coVerify(exactly = 1) { innboksRepository.getInnboksWithEksternVarslingForEventIds(any()) }
        coVerify(exactly = 1) { doknotStatusRepository.updateStatusesForInnboks(any()) }
        coVerify(exactly = 0) { beskjedRepository.getBeskjedWithEksternVarslingForEventIds(any()) }
    }

    @Test
    fun `should update if status already exists for innboks`() {

        val newStatus1 = createDoknotifikasjonStatusDto("innboks",kanaler = listOf("SMS"))

        val newStatus2 = createDoknotifikasjonStatusDto(
            newStatus1.eventId,
            kanaler = listOf("SMS"),
            status = "newestStatus",
            melding = "newestMelding"
        )

        val innboks = giveMeInnboksWithEventIdAndAppnavn(newStatus1.eventId, newStatus1.bestillerAppnavn)

        val existingStatus = createDoknotifikasjonStatusDto(
            eventId = newStatus1.eventId,
            antallOppdateringer = 3,
            kanaler = listOf("EPOST")
        )

        coEvery {
            innboksRepository.getInnboksWithEksternVarslingForEventIds(any())
        } returns listOf(innboks)

        val persistResult = createPersistActionResult(
            successful = listOf(newStatus2),
            unchanged = emptyList()
        )

        val persistedSlot = slot<List<DoknotifikasjonStatusDto>>()

        coEvery {
            doknotStatusRepository.getStatusesForInnboks(listOf(newStatus1.eventId))
        } returns listOf(existingStatus)

        coEvery {
            doknotStatusRepository.updateStatusesForInnboks(capture(persistedSlot))
        } returns persistResult

        runBlocking {
            statusUpdater.updateStatusForInnboks(listOf(newStatus1, newStatus2))
        }

        val persistedStatus = persistedSlot.captured[0]

        persistedStatus.status shouldBe newStatus2.status
        persistedStatus.melding shouldBe newStatus2.melding
        persistedStatus.kanaler.size shouldBe 2
        persistedStatus.kanaler shouldContainAll listOf("SMS", "EPOST")
        persistedStatus.antallOppdateringer shouldBe 5
    }

}
