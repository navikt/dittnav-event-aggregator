package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import java.time.LocalDateTime

data class BeskjedArchiveDTO(
    val fodselsnummer: String,
    val eventId: String,
    val tekst: String,
    val link: String,
    val sikkerhetsnivaa: Int,
    val aktiv: Boolean,
    val eksternVarslingSendt: Boolean,
    val eksternVarslingKanaler: String,
    val forstBehandlet: LocalDateTime
)
