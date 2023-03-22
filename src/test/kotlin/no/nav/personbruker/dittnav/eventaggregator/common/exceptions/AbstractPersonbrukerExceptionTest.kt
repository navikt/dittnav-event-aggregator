package no.nav.personbruker.dittnav.eventaggregator.common.exceptions

import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import no.nav.personbruker.dittnav.eventaggregator.common.database.RetriableDatabaseException
import org.junit.jupiter.api.Test

class AbstractPersonbrukerExceptionTest {

    @Test
    fun `Skal skrive ut innholdet av context i toString-metoden`() {
        val key1 = "key1"
        val key2 = "key2"
        val value1 = "value1"
        val value2 = "value2"
        val message = "A message"
        val exception = RetriableDatabaseException(message, null)
        exception.addContext(key1, value1)
        exception.addContext(key2, value2)

        val toStringForException = exception.toString()

        toStringForException shouldContain "context:"
        toStringForException shouldContain key1
        toStringForException shouldContain key2
        toStringForException shouldContain value1
        toStringForException shouldContain value2
        toStringForException shouldContain message
    }

    @Test
    fun `Skal bruke standard toString hvis det ikke er lagt ved noe context`() {
        val message = "A message"
        val exception = RetriableDatabaseException(message, null)

        exception.toString() shouldNotContain "context:"
    }

}