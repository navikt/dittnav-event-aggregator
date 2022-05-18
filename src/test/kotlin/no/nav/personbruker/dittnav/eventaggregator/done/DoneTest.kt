package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

internal class DoneTest {

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val done = DoneObjectMother.giveMeDone("dummyEventId", "dummProdusent", "123")
        val doneAsString = done.toString()
        doneAsString shouldContain "fodselsnummer=***"
    }

}
