package no.nav.personbruker.dittnav.eventaggregator.oppgave.archive

import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.archive.ArchiveMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.OPPGAVE_INTERN
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Duration.ofMillis

internal class PeriodicOppgaveArchiverTest {

    private val repository: OppgaveArchivingRepository = mockk()
    private val probe: ArchiveMetricsProbe = mockk()
    private val ageThresholdDays = 10

    @AfterEach
    fun resetMocks() {
        clearMocks(repository)
        clearMocks(probe)
    }

    @Test
    fun `archiver should initialize in paused state`() = runBlocking {
        val archiver = PeriodicOppgaveArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        delay(100)

        coVerify(exactly = 0) { repository.getOldOppgaveAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe false
    }

    @Test
    fun `archiver should start when prompted`() = runBlocking {
        coEvery {
           repository.getOldOppgaveAsArchiveDto(any())
        } returns emptyList()

        val archiver = PeriodicOppgaveArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        archiver.start()
        delay(100)
        archiver.stop()

        coVerify(atLeast = 2) { repository.getOldOppgaveAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe true
    }

    @Test
    fun `archiver should wait for specified time between each time checking for oppgave to archive`() = runBlocking {
        coEvery {
            repository.getOldOppgaveAsArchiveDto(any())
        } returns emptyList()

        val archiver = PeriodicOppgaveArchiver(repository, probe, ageThresholdDays, ofMillis(100))

        archiver.start()
        delay(50)
        archiver.stop()

        coVerify(exactly = 1) { repository.getOldOppgaveAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe true
    }

    @Test
    fun `archiver should move oppgave to archive if old events were found`() = runBlocking {
        val toArchive = listOf(mockk<BrukernotifikasjonArchiveDTO>())

        var firstCall = true

        coEvery {
            repository.getOldOppgaveAsArchiveDto(any())
        } answers {
            if (firstCall) {
                firstCall = false
                toArchive
            } else {
                emptyList()
            }
        }

        coEvery { repository.moveToOppgaveArchive(any()) } returns Unit
        coEvery { probe.countEntitiesArchived(any(), any()) } returns Unit

        val archiver = PeriodicOppgaveArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        archiver.start()
        delay(100)
        archiver.stop()

        coVerify(atLeast = 1) { repository.getOldOppgaveAsArchiveDto(any()) }
        coVerify(exactly = 1) { repository.moveToOppgaveArchive(toArchive) }
        coVerify(exactly = 1) { probe.countEntitiesArchived(OPPGAVE_INTERN, toArchive) }
    }
}
