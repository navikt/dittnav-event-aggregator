package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

class KafkaTopicEventCounterService(val environment: Environment) {

    private val log = LoggerFactory.getLogger(KafkaTopicEventCounterService::class.java)

    fun countEventsOnTopic(topicToCount: String, eventType: EventType): Long {
        val consumer = initConsumer(eventType, topicToCount)
        return try {
            countEvents(consumer, eventType)
        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall eventer på topic: $topicToCount", e)
            -1
        } finally {
            closeConsumer(consumer)
        }
    }

    private fun initConsumer(eventType: EventType, topicToCount: String): KafkaConsumer<Nokkel, *> {
        return when (eventType) {
            EventType.BESKJED -> createCountConsumer<Beskjed>(eventType, topicToCount, environment)
            EventType.OPPGAVE -> createCountConsumer<Oppgave>(eventType, topicToCount, environment)
            EventType.INNBOKS -> createCountConsumer<Innboks>(eventType, topicToCount, environment)
            EventType.DONE -> createCountConsumer<Done>(eventType, topicToCount, environment)
        }
    }
}
