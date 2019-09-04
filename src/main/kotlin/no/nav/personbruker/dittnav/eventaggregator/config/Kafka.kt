package no.nav.personbruker.dittnav.eventaggregator.config

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.netty.util.NetUtil.getHostname
import no.nav.personbruker.dittnav.eventaggregator.config.ConfigUtil.isCurrentlyRunningOnNais
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetSocketAddress
import java.util.*

object Kafka {

    private val log: Logger = LoggerFactory.getLogger(Kafka::class.java)

    // Har midlertidig lag på et -testing postfix på topic-navene, slik at vi ikke ved et uhell kludrer til de reelle topic-ene.
    val doneTopicName = "aapen-brukernotifikasjon-done-v1-testing"
    val oppgaveTopicName = "aapen-brukernotifikasjon-nyOppgave-v1-testing"
    val meldingTopicName = "aapen-brukernotifikasjon-nyMelding-v1-testing"
    val informasjonTopicName = "aapen-brukernotifikasjon-nyInformasjon-v1-testing" // Kun denne topic-en som foreløpig er opprettet

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

    fun consumerProps(env: Environment, eventTypeToConsume : String): Properties {
        val groupIdAndEventType = env.groupId + eventTypeToConsume
        return Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.bootstrapServers)
            put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, env.schemaRegistryUrl)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupIdAndEventType)
            put(ConsumerConfig.CLIENT_ID_CONFIG, groupIdAndEventType + getHostname(InetSocketAddress(0)))
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java)
            put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)
            if (isCurrentlyRunningOnNais()) {
                putAll(credentialProps(env))
            }
        }
    }

    fun producerProps(env: Environment): Properties {
        return Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.bootstrapServers)
            put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, env.schemaRegistryUrl)
            put(ConsumerConfig.CLIENT_ID_CONFIG, env.groupId + getHostname(InetSocketAddress(0)))
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            if (isCurrentlyRunningOnNais()) {
                putAll(credentialProps(env))
            }
        }
    }

}
