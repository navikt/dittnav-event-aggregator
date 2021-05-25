package no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import org.apache.kafka.clients.consumer.ConsumerRecord

fun <T> ConsumerRecord<NokkelIntern, T>.getNonNullKey(): NokkelIntern {
    return key() ?: throw NokkelNullException("Produsenten har ikke spesifisert en kafka-key for sitt event")
}
