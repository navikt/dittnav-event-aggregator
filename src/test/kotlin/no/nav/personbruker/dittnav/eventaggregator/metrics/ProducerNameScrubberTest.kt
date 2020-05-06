package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

internal class ProducerNameScrubberTest {

    private val systemUser = "sys-t-user"
    private val producerNameAlias = "test-user"
    private val producerNameResolver =  mockk<ProducerNameResolver>()
    private val nameScrubber = ProducerNameScrubber(producerNameResolver)

    @BeforeAll
    fun setupMocks() {
        coEvery { producerNameResolver.getProducerNameAlias(systemUser) } returns producerNameAlias
    }

    @Test
    fun shouldUseAvailableAliasForProducerIfFound() {
        runBlocking {
            val scrubbedName = nameScrubber.getPublicAlias(systemUser)

            assertEquals(producerNameAlias, scrubbedName)
            assertNotSame(systemUser, scrubbedName)
        }
    }

    @Test
    fun shouldUseGenericNonSystemAliasIfNotFoundAndNameResemblesIdent() {
        val unknownSystemUser = "srvabcdefgh"
        coEvery { producerNameResolver.getProducerNameAlias(unknownSystemUser) } returns null
        runBlocking {
            val scrubbedName = nameScrubber.getPublicAlias(unknownSystemUser)

            assertEquals(nameScrubber.GENERIC_SYSTEM_USER, scrubbedName)
            assertNotSame(systemUser, scrubbedName)
        }
    }

    @Test
    fun shouldUseGenericSystemAliasIfNotFound() {
        val unknownSystemUser = "dummy"
        coEvery { producerNameResolver.getProducerNameAlias(unknownSystemUser) } returns null
        runBlocking {
            val scrubbedName = nameScrubber.getPublicAlias(unknownSystemUser)

            assertEquals(nameScrubber.UNKNOWN_USER, scrubbedName)
            assertNotSame(systemUser, scrubbedName)
        }
    }
}
