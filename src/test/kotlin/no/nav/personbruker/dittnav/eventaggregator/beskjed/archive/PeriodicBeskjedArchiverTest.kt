package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.archive.ArchiveMetricsProbe
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
        val archiver = PeriodicBeskjedArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        delay(100)

        coVerify(exactly = 0) { repository.getOldBeskjedAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe false
    }

    @Test
    fun `archiver should start when prompted`() = runBlocking {
        coEvery {
           repository.getOldBeskjedAsArchiveDto(any())
        } returns emptyList()

        val archiver = PeriodicBeskjedArchiver(repository, probe, ageThresholdDays, ofMillis(10))

        archiver.start()
        delay(100)
        archiver.stop()

        coVerify(atLeast = 2) { repository.getOldBeskjedAsArchiveDto(any()) }

        archiver.isCompleted() shouldBe true
    }
}
