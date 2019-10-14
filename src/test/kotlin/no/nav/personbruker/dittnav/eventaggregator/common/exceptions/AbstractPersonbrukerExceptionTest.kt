package no.nav.personbruker.dittnav.eventaggregator.common.exceptions

import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not contain`
import org.junit.jupiter.api.Test

class AbstractPersonbrukerExceptionTest {

    @Test
    fun `Skal skrive ut innholdet av context i toString-metoden`() {
        val key1 = "key1"
        val key2 = "key2"
        val value1 = "value1"
        val value2 = "value2"
        val message = "A message"
        val exception = RetriableDatabaseException(message)
        exception.addContext(key1, value1)
        exception.addContext(key2, value2)

        val toStringForException = exception.toString()

        toStringForException `should contain` "context:"
        toStringForException `should contain` key1
        toStringForException `should contain` key2
        toStringForException `should contain` value1
        toStringForException `should contain` value2
        toStringForException `should contain` message
    }

    @Test
    fun `Skal bruke standard toString hvis det ikke er lagt ved noe context`() {
        val message = "A message"
        val exception = RetriableDatabaseException(message)

        exception.toString() `should not contain` "context:"
    }

}