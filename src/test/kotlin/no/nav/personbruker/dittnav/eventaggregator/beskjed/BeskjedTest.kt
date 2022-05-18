package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class BeskjedTest {

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed("dummyEventId", "123")
        val beskjedAsString = beskjed.toString()
        beskjedAsString shouldContain "fodselsnummer=***"
        beskjedAsString shouldContain "tekst=***"
        beskjedAsString shouldContain "link=***"
    }
}
