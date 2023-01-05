package no.nav.personbruker.dittnav.eventaggregator.archive

import java.time.LocalDateTime

data class VarselArchiveDTO(
    val fodselsnummer: String,
    val eventId: String,
    val tekst: String,
    val link: String,
    val sikkerhetsnivaa: Int,
    val aktiv: Boolean,
    val produsentApp: String,
    val eksternVarslingSendt: Boolean,
    val eksternVarslingKanaler: String,
    val forstBehandlet: LocalDateTime
)
