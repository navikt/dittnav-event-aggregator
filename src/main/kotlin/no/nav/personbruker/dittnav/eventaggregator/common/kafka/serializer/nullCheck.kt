package no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldNullException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import org.apache.kafka.clients.consumer.ConsumerRecord

fun<T> ConsumerRecord<Nokkel, T>.getNonNullKey(): Nokkel {
    return key()?: throw NokkelNullException()
}

fun getNonNullField(field: String, fieldName: String): String {
    if(field.isNullOrBlank()) {
        throw FieldNullException("$fieldName var null eller tomt.")
    }
    return field
}