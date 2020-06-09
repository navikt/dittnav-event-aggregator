package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

data class UniqueKafkaEventIdentifier(val eventId: String, val systembruker: String, val fodselsnummer: String)
