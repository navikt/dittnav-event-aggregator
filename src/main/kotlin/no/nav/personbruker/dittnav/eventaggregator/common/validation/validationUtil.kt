package no.nav.personbruker.dittnav.eventaggregator.common.validation

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldValidationException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun validateNonNullFieldMaxLength(field: String, fieldName: String, maxLength: Int): String {
    validateNonNullField(field, fieldName)
    return validateMaxLength(field, fieldName, maxLength)
}

fun validateMaxLength(field: String, fieldName: String, maxLength: Int): String {
    if (field.length > maxLength) {
        val fve = FieldValidationException("Feltet $fieldName kan ikke inneholde mer enn $maxLength tegn.")
        fve.addContext("rejectedField", field)
        throw fve
    }
    return field
}

fun validateNonNullField(field: String?, fieldName: String): String {
    if (field.isNullOrBlank()) {
        throw FieldValidationException("$fieldName var null eller tomt.")
    }
    return field
}

fun timestampToUTCDateOrNull(timestamp: Long): LocalDateTime {
    return timestamp.let { datetime ->
        LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("UTC"))
    }
}

fun validateSikkerhetsnivaa(sikkerhetsnivaa: Int): Int {
    return when (sikkerhetsnivaa) {
        3, 4 -> sikkerhetsnivaa
        else -> throw FieldValidationException("Sikkerhetsnivaa kan bare være 3 eller 4.")
    }
}
