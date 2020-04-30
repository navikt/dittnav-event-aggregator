package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

internal class ProducerNameScrubberTest {

    private val producerName = "sys-t-user"
    private val producerAlias = "test-user"
    private val producerNameResolver =  mockk<ProducerNameResolver>()
    private val nameScrubber = ProducerNameScrubber(producerNameResolver)

    @BeforeAll
    fun setupMocks() {
        every { producerNameResolver.getProducerNameAliases() } returns mapOf(producerName to producerAlias)
    }

    @Test
    fun shouldUseAvailableAliasForProducerIfFound() {
        val scrubbedName = nameScrubber.getPublicAlias(producerName)

        assertEquals(producerAlias, scrubbedName)
        assertNotSame(producerName, scrubbedName)
    }

    @Test
    fun shouldUseGenericNonSystemAliasIfNotFoundAndNameResemblesIdent() {
        val scrubbedName = nameScrubber.getPublicAlias("srvabcdefgh")

        assertEquals(nameScrubber.GENERIC_SYSTEM_USER, scrubbedName)
        assertNotSame(producerName, scrubbedName)
    }

    @Test
    fun shouldUseGenericSystemAliasIfNotFound() {
        val scrubbedName = nameScrubber.getPublicAlias("dummy")

        assertEquals(nameScrubber.UNKNOWN_USER, scrubbedName)
        assertNotSame(producerName, scrubbedName)
    }

    @Test
    fun shouldUseGenericSystemAliasIfAliasListIsEmpty() {
        every { producerNameResolver.getProducerNameAliases() } returns emptyMap()
        val scrubbedName = nameScrubber.getPublicAlias("dummy")

        assertEquals(nameScrubber.UNKNOWN_USER, scrubbedName)
        assertNotSame(producerName, scrubbedName)
    }
}
