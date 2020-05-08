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

    private val repository = mockk<BrukernotifikasjonRepository<Beskjed>>(relaxed = true)
    private val service = BrukernotifikasjonPersistingService(repository)

    private val entities = BeskjedObjectMother.giveMeTwoAktiveBeskjeder()

    @BeforeEach
    fun `reset mocks`() {
        clearMocks(repository)
    }

    @AfterAll
    private fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `event skrives som batch hvis alt gaar bra`() {
        runBlocking {
            service.writeEventsToCache(entities)
        }

        coVerify(exactly = 1) { repository.createInOneBatch(any()) }
        coVerify(exactly = 0) { repository.createOneByOneToFilterOutTheProblematicEvents(any()) }

        confirmVerified(repository)
    }

    @Test
    fun `hvis det skjer en AggregatorBatchUpdateException saa skal det forsokes aa skrive eventer en etter en til databasen`() {
        coEvery {
            repository.createInOneBatch(any())
        } throws AggregatorBatchUpdateException("Simulert feil i en test")

        runBlocking {
            service.writeEventsToCache(entities)
        }

        coVerify(exactly = 1) { repository.createInOneBatch(any()) }
        coVerify(exactly = 1) { repository.createOneByOneToFilterOutTheProblematicEvents(any()) }

        confirmVerified(repository)
    }

    @Test
    fun `uventede exceptions skal boble videre`() {
        coEvery {
            repository.createInOneBatch(any())
        } throws RetriableDatabaseException("Simulert feil i en test")

        invoking {
            runBlocking {
                service.writeEventsToCache(entities)
            }
        } `should throw` RetriableDatabaseException::class

        coVerify(exactly = 1) { repository.createInOneBatch(any()) }
        coVerify(exactly = 0) { repository.createOneByOneToFilterOutTheProblematicEvents(any()) }

        confirmVerified(repository)
    }

}
