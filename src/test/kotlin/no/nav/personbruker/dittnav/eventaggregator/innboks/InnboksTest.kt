package no.nav.personbruker.dittnav.eventaggregator.innboks

import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class InnboksTest {

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val innboks = InnboksTestData.innboks("dummyEventId", "123")
        val innboksAsString = innboks.toString()
        innboksAsString shouldContain "fodselsnummer=***"
        innboksAsString shouldContain "tekst=***"
        innboksAsString shouldContain "link=***"
    }

}
