package no.nav.personbruker.dittnav.eventaggregator.common.database

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.AggregatorBatchUpdateException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BrukernotifikasjonPersistingServiceTest {

    private val beskjedRepository = mockk<BrukernotifikasjonRepository<Beskjed>>(relaxed = true)
    private val beskjedService = BrukernotifikasjonPersistingService(beskjedRepository)

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
    fun `event skrives som batch hvis alt gaar bra`() {
        runBlocking {
            beskjedService.writeEventsToCache(entities)
        }

        coVerify(exactly = 1) { beskjedRepository.createInOneBatch(any()) }
        coVerify(exactly = 0) { beskjedRepository.createOneByOneToFilterOutTheProblematicEvents(any()) }
    }

    @Test
    fun `hvis det skjer en AggregatorBatchUpdateException saa skal det forsokes aa skrive eventer en etter en til databasen`() {
        coEvery {
            beskjedRepository.createInOneBatch(any())
        } throws AggregatorBatchUpdateException("Simulert feil i en test")

        runBlocking {
            beskjedService.writeEventsToCache(entities)
        }

        coVerify(exactly = 1) { beskjedRepository.createInOneBatch(any()) }
        coVerify(exactly = 1) { beskjedRepository.createOneByOneToFilterOutTheProblematicEvents(any()) }
    }

    @Test
    fun `uventede exceptions skal boble videre`() {
        coEvery {
            beskjedRepository.createInOneBatch(any())
        } throws RetriableDatabaseException("Simulert feil i en test")

        invoking {
            runBlocking {
                beskjedService.writeEventsToCache(entities)
            }
        } `should throw` RetriableDatabaseException::class

        coVerify(exactly = 1) { beskjedRepository.createInOneBatch(any()) }
        coVerify(exactly = 0) { beskjedRepository.createOneByOneToFilterOutTheProblematicEvents(any()) }
    }

}
