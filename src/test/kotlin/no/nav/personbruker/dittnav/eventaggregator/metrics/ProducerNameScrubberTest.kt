package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class ProducerNameScrubberTest {

    private val systemUser = "sys-t-user"
    private val producerNameAlias = "test-user"
    private val producerNameResolver = mockk<ProducerNameResolver>()
    private val nameScrubber = ProducerNameScrubber(producerNameResolver)

    @BeforeAll
    fun setupMocks() {
        coEvery { producerNameResolver.getProducerNameAlias(systemUser) } returns producerNameAlias
    }

    @Test
    fun shouldUseAvailableAliasForProducerIfFound() {
        runBlocking {
            val scrubbedName = nameScrubber.getPublicAlias(systemUser)

            scrubbedName `should be equal to` producerNameAlias
            scrubbedName `should not be equal to` systemUser
        }
    }

    @Test
    fun shouldUseGenericNonSystemAliasIfNotFoundAndNameResemblesIdent() {
        val unknownSystemUser = "srvabcdefgh"
        coEvery { producerNameResolver.getProducerNameAlias(unknownSystemUser) } returns null
        runBlocking {
            val scrubbedName = nameScrubber.getPublicAlias(unknownSystemUser)

            scrubbedName `should be equal to` nameScrubber.GENERIC_SYSTEM_USER
            scrubbedName `should not be equal to` systemUser
        }
    }

    @Test
    fun shouldUseGenericSystemAliasIfNotFound() {
        val unknownSystemUser = "dummy"
        coEvery { producerNameResolver.getProducerNameAlias(unknownSystemUser) } returns null
        runBlocking {
            val scrubbedName = nameScrubber.getPublicAlias(unknownSystemUser)

            scrubbedName `should be equal to` nameScrubber.UNKNOWN_USER
            scrubbedName `should not be equal to` systemUser
        }
    }
}
