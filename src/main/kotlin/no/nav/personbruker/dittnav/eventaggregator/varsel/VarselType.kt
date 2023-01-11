package no.nav.personbruker.dittnav.eventaggregator.varsel

enum class VarselType(val eventType: String) {
    OPPGAVE("oppgave"),
    BESKJED("beskjed"),
    INNBOKS("innboks")
}

enum class VarselTable {
    beskjed, oppgave, innboks;

    companion object {
        fun fromVarselType(varselType: VarselType): VarselTable {
            return when (varselType) {
                VarselType.OPPGAVE -> oppgave
                VarselType.BESKJED -> beskjed
                VarselType.INNBOKS -> innboks
            }
        }
    }
}
