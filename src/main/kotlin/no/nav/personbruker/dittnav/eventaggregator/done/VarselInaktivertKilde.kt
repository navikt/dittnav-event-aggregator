package no.nav.personbruker.dittnav.eventaggregator.done

enum class VarselInaktivertKilde {
    Bruker, Produsent, Frist;

    val lowercaseName = name.lowercase()
}
