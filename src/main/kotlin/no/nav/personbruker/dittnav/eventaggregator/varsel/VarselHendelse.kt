package no.nav.personbruker.dittnav.eventaggregator.varsel

data class VarselHendelse(
    val hendelseType: HendelseType,
    val varselType: VarselType,
    val eventId: String,
    val namespace: String,
    val appnavn: String
)

enum class HendelseType {
    Aktivert, Inaktivert;

    val lowerCaseName = name.lowercase()
}
