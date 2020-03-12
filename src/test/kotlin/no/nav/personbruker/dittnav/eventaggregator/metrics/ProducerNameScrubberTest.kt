package no.nav.personbruker.dittnav.eventaggregator.metrics

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

internal class ProducerNameScrubberTest {

    val producerName = "sys-t-user"
    val producerAlias = "test-user"
    val aliasEnvVariable = "$producerName:$producerAlias"

    val nameScrubber = ProducerNameScrubber(aliasEnvVariable)

    @Test
    fun shouldUseAvailableAliasForProducerIfFound() {
        val scrubbedName = nameScrubber.getPublicAlias(producerName)

        assertEquals(producerAlias, scrubbedName)
        assertNotSame(producerName, scrubbedName)
    }

    @Test
    fun shouldUseGenericNonSystemAliasIfNotFoundAndNameResemblesIdent() {
        val scrubbedName = nameScrubber.getPublicAlias("A123456")

        assertEquals(nameScrubber.GENERIC_NON_SYSTEM_USER, scrubbedName)
        assertNotSame(producerName, scrubbedName)
    }

    @Test
    fun shouldUseGenericSystemAliasIfNotFound() {
        val scrubbedName = nameScrubber.getPublicAlias("dummy")

        assertEquals(nameScrubber.GENERIC_SYSTEM_USER, scrubbedName)
        assertNotSame(producerName, scrubbedName)
    }


}