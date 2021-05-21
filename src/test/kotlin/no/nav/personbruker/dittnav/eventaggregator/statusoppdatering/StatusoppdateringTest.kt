package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.Test

class StatusoppdateringTest {

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val statusoppdatering = StatusoppdateringObjectMother.giveMeStatusoppdatering("dummyEventId", "123")
        val statusoppdateringAsString = statusoppdatering.toString()
        statusoppdateringAsString `should contain` "fodselsnummer=***"
        statusoppdateringAsString `should contain` "systembruker=***"
        statusoppdateringAsString `should contain` "link=***"
    }

}