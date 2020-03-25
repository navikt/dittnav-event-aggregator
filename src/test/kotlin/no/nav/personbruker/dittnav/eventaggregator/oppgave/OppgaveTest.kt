package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.done.DoneObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.Test

class OppgaveTest {

    private val expectedEventId = "o-1"
    private val expectedFodselsnr = "1234"
    private val expectedProdusent = "produsent-a"

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val oppgave = OppgaveObjectMother.giveMeOppgave("dummyEventId", "123")
        val oppgaveAsString = oppgave.toString()
        oppgaveAsString `should contain` "fodselsnummer=***"
        oppgaveAsString `should contain` "tekst=***"
        oppgaveAsString `should contain` "link=***"
    }

    @Test
    internal fun `skal match tilhorende done-event`() {
        val beskjed = BeskjedObjectMother.giveMeBeskjed(expectedEventId, expectedFodselsnr, expectedProdusent)
        val matchendeDoneEvent = DoneObjectMother.createDone(expectedEventId, expectedProdusent, expectedFodselsnr)

        beskjed.isRepresentsSameEvent(matchendeDoneEvent) `should be equal to` true
    }

    @Test
    internal fun `skal ikke match med ikke-tilhorende done-eventer`() {
        val oppgave = OppgaveObjectMother.giveMeOppgave(expectedEventId, expectedFodselsnr, expectedProdusent)
        val ikkeMatchendeDoneEvent = DoneObjectMother.createDone(expectedEventId, expectedProdusent, "7654")

        oppgave.isRepresentsSameEvent(ikkeMatchendeDoneEvent) `should be equal to` false
    }

}
