package no.nav.personbruker.dittnav.eventaggregator.common.validation

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldValidationException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private val fodselsnummerRegEx = """[\d]{1,11}""".toRegex()

fun validateFodselsnummer(field: String): String {
    validateNonNullField(field, "fødselsnummer")
    if (isNotValidFodselsnummer(field)) {
        val fve = FieldValidationException("Feltet fodselsnummer kan kun innholde siffer, og maks antall er 11.")
        fve.addContext("rejectedFieldValue", field)
        throw fve
    }
    return field
}

private fun isNotValidFodselsnummer(field: String) = !fodselsnummerRegEx.matches(field)

fun validateNonNullFieldMaxLength(field: String, fieldName: String, maxLength: Int): String {
    validateNonNullField(field, fieldName)
    return validateMaxLength(field, fieldName, maxLength)
}

fun validateMaxLength(field: String, fieldName: String, maxLength: Int): String {
    if (field.length > maxLength) {
        val fve = FieldValidationException("Feltet $fieldName kan ikke inneholde mer enn $maxLength tegn.")
        fve.addContext("rejectedFieldValueLength", field.length)
        throw fve
    }
    return field
}

fun validateNonNullField(field: String?, fieldName: String): String {
    if (field.isNullOrBlank()) {
        val fve = FieldValidationException("$fieldName var null eller tomt.")
        fve.addContext("nullOrBlank", fieldName)
        throw fve
    }
    return field
}

fun timestampToUTCDateOrNull(timestamp: Long?): LocalDateTime? {
    return timestamp?.let { datetime ->
        LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("UTC"))
    }
}

fun validateSikkerhetsnivaa(sikkerhetsnivaa: Int): Int {
    return when (sikkerhetsnivaa) {
        3, 4 -> sikkerhetsnivaa
        else -> throw FieldValidationException("Sikkerhetsnivaa kan bare være 3 eller 4.")
    }
}

fun validateStatusGlobal(statusGlobal: String): String {
    return when (statusGlobal) {
        "SENDT", "MOTTATT", "UNDER_BEHANDLING", "FERDIG" -> statusGlobal
        else -> throw FieldValidationException("StatusGlobal kan kun inneholde SENDT, MOTTATT, UNDER_BEHANDLING eller FERDIG.")
    }
}
