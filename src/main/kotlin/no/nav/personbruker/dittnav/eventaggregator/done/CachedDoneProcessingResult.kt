package no.nav.personbruker.dittnav.eventaggregator.done

data class CachedDoneProcessingResult(
        val eventsMatchingBeskjed: List<Done>,
        val eventsMatchingOppgave: List<Done>,
        val eventsMatchingInnboks: List<Done>,
        val eventsMatchingNone: List<Done>
) {
    val eventsMatchingAny get() = listOf(eventsMatchingBeskjed, eventsMatchingOppgave, eventsMatchingInnboks)
            .flatten()
            .distinct()

    val allDoneEvents get() = eventsMatchingAny + eventsMatchingNone

    override fun toString(): String {
        val antallDoneEventer = allDoneEvents.size
        val antallBrukernotifikasjoner = eventsMatchingBeskjed.size + eventsMatchingOppgave.size + eventsMatchingInnboks.size

        return """
            Prosesserte $antallDoneEventer done-eventer, opp mot $antallBrukernotifikasjoner brukernotifikasjoner:
            Fant ${eventsMatchingBeskjed.size} done-eventer med tilhørende eventer i beskjed-tabellen.
            Fant ${eventsMatchingInnboks.size} done-eventer med tilhørende eventer i innboks-tabellen.
            Fant ${eventsMatchingOppgave.size} done-eventer med tilhørende eventer i oppgave-tabellen.
            Det er ${eventsMatchingNone.size} done-eventer det ikke ble funnet et tilhørende event for nå.
        """.trimIndent()
    }
}