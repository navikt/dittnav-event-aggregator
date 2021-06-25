package no.nav.personbruker.dittnav.eventaggregator.beskjed

import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.Test

class BeskjedTest {

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed("dummyEventId", "123")
        val beskjedAsString = beskjed.toString()
        beskjedAsString `should contain` "fodselsnummer=***"
        beskjedAsString `should contain` "tekst=***"
        beskjedAsString `should contain` "link=***"
    }
}
