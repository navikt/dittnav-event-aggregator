package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.done.DoneObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.Test

class BeskjedTest {

    private val expectedEventId = "b-1"
    private val expectedFodselsnr = "1234"
    private val expectedProdusent = "produsent-a"

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val beskjed = BeskjedObjectMother.giveMeBeskjed("dummyEventId", "123")
        val beskjedAsString = beskjed.toString()
        beskjedAsString `should contain` "fodselsnummer=***"
        beskjedAsString `should contain` "tekst=***"
        beskjedAsString `should contain` "link=***"
    }

    @Test
    internal fun `skal match tilhorende done-event`() {
        val beskjed = BeskjedObjectMother.giveMeBeskjed(expectedEventId, expectedFodselsnr, expectedProdusent)
        val matchendeDoneEvent = DoneObjectMother.createDone(expectedEventId, expectedProdusent, expectedFodselsnr)

        beskjed.isRepresentsSameEvent(matchendeDoneEvent) `should be equal to` true
    }

    @Test
    internal fun `skal ikke match med ikke-tilhorende done-eventer`() {
        val beskjed = BeskjedObjectMother.giveMeBeskjed(expectedEventId, expectedFodselsnr, expectedProdusent)
        val ikkeMatchendeDoneEvent = DoneObjectMother.createDone("annenEventId", expectedProdusent, expectedFodselsnr)

        beskjed.isRepresentsSameEvent(ikkeMatchendeDoneEvent) `should be equal to` false
    }

}
