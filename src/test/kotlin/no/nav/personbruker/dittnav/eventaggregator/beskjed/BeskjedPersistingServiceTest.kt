package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.ConstraintViolationDatabaseException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BeskjedPersistingServiceTest {

    private val beskjedRepository = mockk<BeskjedRepository>(relaxed = true)
    private val beskjedService = BeskjedPersistingService(beskjedRepository)

    private val entities = BeskjedObjectMother.giveMeTwoAktiveBeskjeder()

    @BeforeEach
    fun `reset mocks`() {
        clearMocks(beskjedRepository)
    }

    @AfterAll
    private fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `beskjeder skrives som batch hvis alt gaar bra`() {
        runBlocking {
            beskjedService.writeEventsToCache(entities)
        }

        coVerify(exactly = 1) { beskjedRepository.createBeskjederIEnBatch(any()) }
        coVerify(exactly = 0) { beskjedRepository.createOneByOneToFilterOutTheProblematicEvent(any()) }
    }

    @Test
    fun `beskjeder skrives en etter en hvis batch skriving feiler`() {
        coEvery {
            beskjedRepository.createBeskjederIEnBatch(any())
        } throws ConstraintViolationDatabaseException("Simulert feil i en test")

        runBlocking {
            beskjedService.writeEventsToCache(entities)
        }

        coVerify(exactly = 1) { beskjedRepository.createBeskjederIEnBatch(any()) }
        coVerify(exactly = 1) { beskjedRepository.createOneByOneToFilterOutTheProblematicEvent(any()) }
    }

}
