package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullField
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object BeskjedTransformer {

    private val MAX_TEXT_FIELD_LENGTH = 500

    fun toInternal(externalNokkel: Nokkel, externalValue: no.nav.brukernotifikasjon.schemas.Beskjed): Beskjed {
        val newRecordsAreActiveByDefault = true
        val internal = Beskjed(
                createRandomStringUUID(),
                externalNokkel.getSystembruker(),
                externalNokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(externalValue.getTidspunkt()), ZoneId.of("UTC")),
                getNonNullField(externalValue.getFodselsnummer(), "Fødselsnummer"),
                externalValue.getGrupperingsId(),
                externalValue.getTekstIfValid(),
                externalValue.getLink(),
                externalValue.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                externalValue.getAsUTDateTime(),
                newRecordsAreActiveByDefault
        )
        return internal
    }

    private fun no.nav.brukernotifikasjon.schemas.Beskjed.getAsUTDateTime(): LocalDateTime? {
        return getSynligFremTil()?.let { datetime -> LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("UTC")) }
    }

    private fun no.nav.brukernotifikasjon.schemas.Beskjed.getTekstIfValid(): String {
        if (getTekst().isNullOrBlank()) {
            throw FieldValidationException("Feltet tekst må ha en verdi satt, men var null eller blank.")

        } else if (getTekst().length > MAX_TEXT_FIELD_LENGTH) {
            val fve = FieldValidationException("Feltet tekst kan ikke inneholde mer enn $MAX_TEXT_FIELD_LENGTH tegn.")
            fve.addContext("rejectedField", getTekst())
            throw fve
        }
        return getTekst()
    }

    private fun createRandomStringUUID(): String {
        return UUID.randomUUID().toString()
    }
}
