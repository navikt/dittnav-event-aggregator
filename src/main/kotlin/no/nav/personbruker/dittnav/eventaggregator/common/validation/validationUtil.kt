package no.nav.personbruker.dittnav.eventaggregator.common.validation

import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.builders.exception.FieldValidationException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun timestampToUTCDateOrNull(timestamp: Long?): LocalDateTime? {
    return timestamp?.let { datetime ->
        LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("UTC"))
    }
}

fun validatePrefererteKanaler(eksternVarsling: Boolean, field: List<String>): List<String> {
    val fieldName = "prefererteKanaler"
    if (!eksternVarsling && field.isNotEmpty()) {
        throw FieldValidationException("Feltet $fieldName kan ikke settes så lenge eksternVarsling er false.")
    } else {
        try {
            field.forEach { preferertKanal -> PreferertKanal.valueOf(preferertKanal) }
        } catch(e: IllegalArgumentException) {
            throw FieldValidationException("Feltet $fieldName kan bare innholde følgende verdier: ${PreferertKanal.values()}")
        }
    }
    return field
}
