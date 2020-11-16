package no.nav.personbruker.dittnav.eventaggregator.config

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.netty.util.NetUtil.getHostname
import no.nav.personbruker.dittnav.common.util.kafka.serializer.SwallowSerializationErrorsAvroDeserializer
import no.nav.personbruker.dittnav.eventaggregator.config.ConfigUtil.isCurrentlyRunningOnNais
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetSocketAddress
import java.util.*

object Kafka {

    private val log: Logger = LoggerFactory.getLogger(Kafka::class.java)

    val doneTopicName = "aapen-brukernotifikasjon-done-v1"
    val beskjedTopicName = "aapen-brukernotifikasjon-nyBeskjed-v1"
    val innboksTopicName = "aapen-brukernotifikasjon-nyInnboks-v1"
    val oppgaveTopicName = "aapen-brukernotifikasjon-nyOppgave-v1"
    val statusoppdateringTopicName = "aapen-brukernotifikasjon-nyStatusoppdatering-v1"

    private fun credentialProps(env: Environment): Properties {
        return Properties().apply {
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
            put(SaslConfigs.SASL_JAAS_CONFIG,
                    """org.apache.kafka.common.security.plain.PlainLoginModule required username="${env.username}" password="${env.password}";""")
            System.getenv("NAV_TRUSTSTORE_PATH")?.let {
                put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
                put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, File(it).absolutePath)
                put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, System.getenv("NAV_TRUSTSTORE_PASSWORD"))
                log.info("Configured ${SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG} location")
            }
        }
    }

    fun consumerProps(env: Environment, eventTypeToConsume: EventType, enableSecurity: Boolean = isCurrentlyRunningOnNais()): Properties {
        val groupIdAndEventType = buildGroupIdIncludingEventType(env, eventTypeToConsume)
        return Properties().apply {
            put(ConsumerConfig.GROUP_ID_CONFIG, groupIdAndEventType)
            put(ConsumerConfig.CLIENT_ID_CONFIG, groupIdAndEventType + getHostname(InetSocketAddress(0)))
            commonProps(env, enableSecurity)
        }
    }

    fun counterConsumerProps(env: Environment, eventTypeToConsume: EventType, enableSecurity: Boolean = isCurrentlyRunningOnNais()): Properties {
        val groupIdAndEventType = "dn-aggregator_metrics_counter_" + eventTypeToConsume.eventType
        val sixMinutes = 6 * 60 * 1000
        return Properties().apply {
            put(ConsumerConfig.GROUP_ID_CONFIG, groupIdAndEventType)
            put(ConsumerConfig.CLIENT_ID_CONFIG, groupIdAndEventType + getHostname(InetSocketAddress(0)))
            commonProps(env, enableSecurity)
            put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, sixMinutes)
        }
    }

    private fun Properties.commonProps(env: Environment, enableSecurity: Boolean) {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.bootstrapServers)
        put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, env.schemaRegistryUrl)
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, SwallowSerializationErrorsAvroDeserializer::class.java)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SwallowSerializationErrorsAvroDeserializer::class.java)
        put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)
        if (enableSecurity) {
            putAll(credentialProps(env))
        }
    }

    private fun buildGroupIdIncludingEventType(env: Environment, eventTypeToConsume: EventType) =
            env.groupId + eventTypeToConsume.eventType

}
