package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.Produsent
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.SQLException

internal class ProducerNameResolverTest {

    private val database = mockk<Database>()
    private var producerNameResolver = ProducerNameResolver(database)

    @BeforeEach
    fun `setup mocks`() {
        coEvery {
            database.queryWithExceptionTranslation<List<Produsent>>(any())
        }.returns(listOf(Produsent("x-dittnav", "dittnav")))
    }

    @AfterEach
    fun `reset mocks`() {
        clearMocks(database)
        producerNameResolver = ProducerNameResolver(database)
    }

    @Test
    fun `skal returnere produsentnavn`() {
        runBlocking {
            val producerNameAlias = producerNameResolver.getProducerNameAlias("x-dittnav")
            producerNameAlias `should equal` "dittnav"
        }
    }

    @Test
    fun `skal returnere cachede produsentnavn hvis henting av nye feiler`() {
        runBlocking {
            val originalProducerNameAlias = producerNameResolver.getProducerNameAlias("x-dittnav")

            coEvery {
                database.queryWithExceptionTranslation<List<Produsent>>(any())
            }.throws(SQLException())

            val newProducerNameAlias = producerNameResolver.getProducerNameAlias("x-dittnav")
            originalProducerNameAlias `should equal` newProducerNameAlias
        }
    }

    @Test
    fun `skal returnere null hvis produsentnavn ikke ble funnet i cache`() {
        runBlocking {
            val unmatchedProducerNameAlias = producerNameResolver.getProducerNameAlias("x-ukjent")
            unmatchedProducerNameAlias.`should be null`()
        }
    }

    @Test
    fun `skal trigge oppdatering av cachen hvis produsentnavn ikke ble funnet i cache`() {
        runBlocking {
            producerNameResolver.getProducerNameAlias("x-ukjent")
            coVerify(exactly = 2) { database.queryWithExceptionTranslation<List<Produsent>>(any()) }
        }
    }
}
