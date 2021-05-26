package no.nav.personbruker.dittnav.eventaggregator.config

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.netty.util.NetUtil
import io.netty.util.NetUtil.getHostname
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.SwallowSerializationErrorsAvroDeserializer
import no.nav.personbruker.dittnav.eventaggregator.config.ConfigUtil.isCurrentlyRunningOnNais
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.*

object Kafka {

    private val log: Logger = LoggerFactory.getLogger(Kafka::class.java)

    val doneHovedTopicName = "min-side.privat-brukernotifikasjon-done-v1"
    val beskjedHovedTopicName = "min-side.privat-brukernotifikasjon-beskjed-v1"
    val innboksTopicName = "aapen-brukernotifikasjon-nyInnboks-v1"
    val oppgaveHovedTopicName = "min-side.privat-brukernotifikasjon-oppgave-v1"
    val statusoppdateringHovedTopicName = "min-side.privat-brukernotifikasjon-statusoppdatering-v1"

    fun counterConsumerProps(env: Environment, eventTypeToConsume: EventType, enableSecurity: Boolean = isCurrentlyRunningOnNais()): Properties {
        val groupIdAndEventType = "dn-aggregator_metrics_counter_" + eventTypeToConsume.eventType
        val sixMinutes = 6 * 60 * 1000
        return Properties().apply {
            put(ConsumerConfig.GROUP_ID_CONFIG, groupIdAndEventType)
            put(ConsumerConfig.CLIENT_ID_CONFIG, groupIdAndEventType + getHostname(InetSocketAddress(0)))
            put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, sixMinutes)
            if (enableSecurity) {
                putAll(credentialPropsAiven(env))
            }
        }
    }

    fun consumerProps(env: Environment, eventtypeToConsume: EventType, enableSecurity: Boolean = isCurrentlyRunningOnNais()): Properties {
        val groupIdAndEventType = buildGroupIdIncludingEventType(env, eventtypeToConsume)
        return Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.aivenBrokers)
            put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, env.aivenSchemaRegistry)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupIdAndEventType)
            put(ConsumerConfig.CLIENT_ID_CONFIG, groupIdAndEventType + NetUtil.getHostname(InetSocketAddress(0)))
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, SwallowSerializationErrorsAvroDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SwallowSerializationErrorsAvroDeserializer::class.java)
            put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)
            if (enableSecurity) {
                putAll(credentialPropsAiven(env))
            }
        }
    }

    private fun credentialPropsAiven(env: Environment): Properties {
        return Properties().apply {
            put(KafkaAvroSerializerConfig.USER_INFO_CONFIG, "${env.aivenSchemaRegistryUser}:${env.aivenSchemaRegistryPassword}")
            put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO")
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, env.aivenTruststorePath)
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, env.aivenCredstorePassword)
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, env.aivenKeystorePath)
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, env.aivenCredstorePassword)
            put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, env.aivenCredstorePassword)
            put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
        }
    }

    private fun buildGroupIdIncludingEventType(env: Environment, eventTypeToConsume: EventType) =
            env.groupId + eventTypeToConsume.eventType

}
