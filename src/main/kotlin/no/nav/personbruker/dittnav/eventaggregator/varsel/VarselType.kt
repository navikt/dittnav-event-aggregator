package no.nav.personbruker.dittnav.eventaggregator.varsel

enum class VarselType(val eventType: String) {
    OPPGAVE("oppgave"),
    BESKJED("beskjed"),
    INNBOKS("innboks")
}
