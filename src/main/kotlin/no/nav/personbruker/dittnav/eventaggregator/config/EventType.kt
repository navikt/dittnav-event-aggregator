package no.nav.personbruker.dittnav.eventaggregator.config

enum class EventType(val eventType: String) {
    OPPGAVE("oppgave"),
    BESKJED("beskjed"),
    INNBOKS("innboks"),
    DONE("done"),
    STATUSOPPDATERING("statusOppdatering")
}
