package no.nav.personbruker.dittnav.eventaggregator.config

enum class EventType(val eventType: String) {
    OPPGAVE("oppgave"),
    INFORMASJON("informasjon"),
    INNBOKS("innboks"),
    DONE("done")
}
