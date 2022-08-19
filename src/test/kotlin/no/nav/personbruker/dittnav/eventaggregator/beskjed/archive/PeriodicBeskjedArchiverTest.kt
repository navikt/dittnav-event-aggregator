package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.archive.ArchiveMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.BESKJED_INTERN
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Duration.ofMillis

internal class PeriodicBeskjedArchiverTest {

    private val repository: BeskjedArchivingRepository = mockk()
    private val probe: ArchiveMetricsProbe = mockk()
    private val ageThresholdDays = 10

    @AfterEach
    fun resetMocks() {
        clearMocks(repository)
        clearMocks(probe)
    }

    @Test
    fun `archiver should initialize in paused state`() = runBlocking {
        val archiver = PeriodicBeskjedArchiver(repository, probe, ageThresholdDays, ofMillis(50))

        delay(100)

        coVerify(exactly = 0) { repository.getOldBeskjedAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe false
    }

    @Test
    fun `archiver should start when prompted`() = runBlocking {
        coEvery {
           repository.getOldBeskjedAsArchiveDto(any())
        } returns emptyList()

        val archiver = PeriodicBeskjedArchiver(repository, probe, ageThresholdDays, ofMillis(50))

        archiver.start()
        delay(100)
        archiver.stop()

        coVerify(atLeast = 2) { repository.getOldBeskjedAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe true
    }

    @Test
    fun `archiver should wait for specified time between each time checking for beskjed to archive`() = runBlocking {
        coEvery {
            repository.getOldBeskjedAsArchiveDto(any())
        } returns emptyList()

        val archiver = PeriodicBeskjedArchiver(repository, probe, ageThresholdDays, ofMillis(100))

        archiver.start()
        delay(50)
        archiver.stop()

        coVerify(exactly = 1) { repository.getOldBeskjedAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe true
    }

    @Test
    fun `archiver should move beskjed to archive if old events were found`() = runBlocking {
        val toArchive = listOf(mockk<BrukernotifikasjonArchiveDTO>())

        var firstCall = true

        coEvery {
            repository.getOldBeskjedAsArchiveDto(any())
        } answers {
            if (firstCall) {
                firstCall = false
                toArchive
            } else {
                emptyList()
            }
        }

        coEvery { repository.moveToBeskjedArchive(any()) } returns Unit
        coEvery { probe.countEntitiesArchived(any(), any()) } returns Unit

        val archiver = PeriodicBeskjedArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        archiver.start()
        delay(100)
        archiver.stop()

        coVerify(atLeast = 1) { repository.getOldBeskjedAsArchiveDto(any()) }
        coVerify(exactly = 1) { repository.moveToBeskjedArchive(toArchive) }
        coVerify(exactly = 1) { probe.countEntitiesArchived(BESKJED_INTERN, toArchive) }
    }
}
