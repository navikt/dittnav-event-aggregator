package no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import org.apache.kafka.clients.consumer.ConsumerRecord

fun<T> ConsumerRecord<Nokkel, T>.getNonNullKey(): Nokkel {
    return key()?: throw NokkelNullException()
}