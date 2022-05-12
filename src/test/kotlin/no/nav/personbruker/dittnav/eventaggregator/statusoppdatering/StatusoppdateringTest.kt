package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class StatusoppdateringTest {

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val statusoppdatering = StatusoppdateringObjectMother.giveMeStatusoppdatering("dummyEventId", "123")
        val statusoppdateringAsString = statusoppdatering.toString()
        statusoppdateringAsString shouldContain "fodselsnummer=***"
        statusoppdateringAsString shouldContain "systembruker=***"
        statusoppdateringAsString shouldContain "link=***"
    }

}