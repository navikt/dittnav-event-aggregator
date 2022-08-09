package no.nav.personbruker.dittnav.eventaggregator.config

enum class EventType(val eventType: String) {
    OPPGAVE_INTERN("oppgave"),
    BESKJED_INTERN("beskjed"),
    INNBOKS_INTERN("innboks"),
    DONE_INTERN("done"),
}
