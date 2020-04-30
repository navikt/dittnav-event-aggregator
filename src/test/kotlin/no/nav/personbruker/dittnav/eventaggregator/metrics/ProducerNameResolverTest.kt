package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.coEvery
import io.mockk.mockk
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.Produsent
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.Test
import java.sql.SQLException

internal class ProducerNameResolverTest {

    private val database = mockk<Database>()
    private val producernameResolver = ProducerNameResolver(database)

    @Test
    fun `skal returnere produsentnavn`() {
        coEvery {
            database.queryWithExceptionTranslation<List<Produsent>>(any())
        }.returns(listOf(Produsent("x-dittnav", "dittnav")))

        val producernameAliases = producernameResolver.getProducerNameAliases()
        producernameAliases.size `should be` 1
        producernameAliases.getValue("x-dittnav") `should be` "dittnav"
    }

    @Test
    fun `skal returnere cachede produsentnavn hvis henting av nye feiler`() {
        coEvery {
            database.queryWithExceptionTranslation<List<Produsent>>(any())
        }.returns(listOf(Produsent("x-dittnav", "dittnav")))
        val originalProducernameAliases = producernameResolver.getProducerNameAliases()

        coEvery {
            database.queryWithExceptionTranslation<List<Produsent>>(any())
        }.throws(SQLException())

        val newProducernameAliases = producernameResolver.getProducerNameAliases()
        originalProducernameAliases `should equal` newProducernameAliases
    }
}
