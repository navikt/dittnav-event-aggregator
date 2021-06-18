package no.nav.personbruker.dittnav.eventaggregator.common.config

import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import java.util.*

object KafkaEmbed {

    fun consumerProps(env: Environment, eventTypeToConsume: EventType): Properties {
        return Kafka.consumerProps(env, eventTypeToConsume).apply {
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }
    }
}
