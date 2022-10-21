package no.nav.personbruker.dittnav.eventaggregator.common.brukernotifikasjon

import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.done.DoneTestData
import org.junit.jupiter.api.Test

class BrukernotifikasjonTest{

    @Test
    internal fun `skal match tilhorende done-event`() {
        val beskjed = BrukernotifikasjonObjectMother.giveMeBeskjed()
        val matchendeDoneEvent = DoneTestData.matchingDoneEvent(beskjed)

        beskjed.isRepresentsSameEventAs(matchendeDoneEvent) shouldBe true
    }

    @Test
    internal fun `skal matche med eventer selv om fodselsnummer er forskjellig`() {
        val beskjed = BrukernotifikasjonObjectMother.giveMeBeskjed()
        val ikkeMatchendeDoneEvent = DoneTestData.done(beskjed.eventId, beskjed.systembruker, "7654")

        beskjed.isRepresentsSameEventAs(ikkeMatchendeDoneEvent) shouldBe true
    }

    @Test
    internal fun `skal ikke matche med andre eventIder`() {
        val beskjed = BrukernotifikasjonObjectMother.giveMeBeskjed()
        val ikkeMatchendeDoneEvent = DoneTestData.done(beskjed.eventId + "1", beskjed.systembruker, beskjed.fodselsnummer)

        beskjed.isRepresentsSameEventAs(ikkeMatchendeDoneEvent) shouldBe false
    }

}
