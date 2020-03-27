package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.BrukernotifikasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.done.DoneObjectMother
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

class BrukernotifikasjonTest{

    @Test
    internal fun `skal match tilhorende done-event`() {
        val beskjed = BrukernotifikasjonObjectMother.giveMeBeskjed()
        val matchendeDoneEvent = DoneObjectMother.giveMeMatchingDoneEvent(beskjed)

        beskjed.isRepresentsSameEventAs(matchendeDoneEvent) `should be equal to` true
    }

    @Test
    internal fun `skal ikke match med ikke-tilhorende done-eventer`() {
        val beskjed = BrukernotifikasjonObjectMother.giveMeBeskjed()
        val ikkeMatchendeDoneEvent = DoneObjectMother.giveMeDone(beskjed.eventId, beskjed.produsent, "7654")

        beskjed.isRepresentsSameEventAs(ikkeMatchendeDoneEvent) `should be equal to` false
    }

}
