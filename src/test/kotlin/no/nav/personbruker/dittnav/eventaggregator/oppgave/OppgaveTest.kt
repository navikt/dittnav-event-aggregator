package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class OppgaveTest {

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val oppgave = OppgaveObjectMother.giveMeAktivOppgave("dummyEventId", "123")
        val oppgaveAsString = oppgave.toString()
        oppgaveAsString shouldContain "fodselsnummer=***"
        oppgaveAsString shouldContain "tekst=***"
        oppgaveAsString shouldContain "link=***"
    }
}
