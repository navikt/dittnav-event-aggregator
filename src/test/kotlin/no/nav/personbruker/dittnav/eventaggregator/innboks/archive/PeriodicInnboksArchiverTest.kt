package no.nav.personbruker.dittnav.eventaggregator.innboks.archive

import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.archive.ArchiveMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.INNBOKS_INTERN
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Duration.ofMillis

internal class PeriodicInnboksArchiverTest {

    private val repository: InnboksArchivingRepository = mockk()
    private val probe: ArchiveMetricsProbe = mockk()
    private val ageThresholdDays = 10

    @AfterEach
    fun resetMocks() {
        clearMocks(repository)
        clearMocks(probe)
    }

    @Test
    fun `archiver should initialize in paused state`() = runBlocking {
        val archiver = PeriodicInnboksArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        delay(100)

        coVerify(exactly = 0) { repository.getOldInnboksAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe false
    }

    @Test
    fun `archiver should start when prompted`() = runBlocking {
        coEvery {
           repository.getOldInnboksAsArchiveDto(any())
        } returns emptyList()

        val archiver = PeriodicInnboksArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        archiver.start()
        delay(100)
        archiver.stop()

        coVerify(atLeast = 2) { repository.getOldInnboksAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe true
    }

    @Test
    fun `archiver should wait for specified time between each time checking for innboks to archive`() = runBlocking {
        coEvery {
            repository.getOldInnboksAsArchiveDto(any())
        } returns emptyList()

        val archiver = PeriodicInnboksArchiver(repository, probe, ageThresholdDays, ofMillis(100))

        archiver.start()
        delay(50)
        archiver.stop()

        coVerify(exactly = 1) { repository.getOldInnboksAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe true
    }

    @Test
    fun `archiver should move innboks to archive if old events were found`() = runBlocking {
        val toArchive = listOf(mockk<BrukernotifikasjonArchiveDTO>())

        var firstCall = true

        coEvery {
            repository.getOldInnboksAsArchiveDto(any())
        } answers {
            if (firstCall) {
                firstCall = false
                toArchive
            } else {
                emptyList()
            }
        }

        coEvery { repository.moveToInnboksArchive(any()) } returns Unit
        coEvery { probe.countEntitiesArchived(any(), any()) } returns Unit

        val archiver = PeriodicInnboksArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        archiver.start()
        delay(100)
        archiver.stop()

        coVerify(atLeast = 1) { repository.getOldInnboksAsArchiveDto(any()) }
        coVerify(exactly = 1) { repository.moveToInnboksArchive(toArchive) }
        coVerify(exactly = 1) { probe.countEntitiesArchived(INNBOKS_INTERN, toArchive) }
    }
}
