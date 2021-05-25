package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import kotlinx.coroutines.withTimeoutOrNull
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.common.JAAS_PLAIN_LOGIN
import no.nav.common.JAAS_REQUIRED
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SaslConfigs
import java.util.*

object KafkaProducerUtil {

    suspend fun kafkaAvroProduce(
            brokersURL: String,
            schemaRegistryUrl: String,
            topic: String,
            user: String,
            pwd: String,
            data: Map<NokkelIntern, GenericRecord>
    ): Boolean =
            try {
                KafkaProducer<NokkelIntern, GenericRecord>(
                        Properties().apply {
                            set(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersURL)
                            set(ProducerConfig.CLIENT_ID_CONFIG, "funKafkaAvroProduce")
                            set(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer")
                            set(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer")
                            set(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
                            set(ProducerConfig.ACKS_CONFIG, "all")
                            set(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1)
                            set(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 500)
                            set(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
                            set(SaslConfigs.SASL_MECHANISM, "PLAIN")
                            set(SaslConfigs.SASL_JAAS_CONFIG, "$JAAS_PLAIN_LOGIN $JAAS_REQUIRED username=\"$user\" password=\"$pwd\";")
                        }
                ).use { p ->
                    withTimeoutOrNull(10_000) {
                        data.forEach { k, v -> p.send(ProducerRecord(topic, k, v)).get() }
                        true
                    } ?: false
                }
            } catch (e: Exception) {
                false
            }

    suspend fun kafkaProduce(
            brokersURL: String,
            topic: String,
            user: String,
            pwd: String,
            data: Map<String, String>
    ): Boolean =
            try {
                KafkaProducer<String, String>(
                        Properties().apply {
                            set(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersURL)
                            set(ProducerConfig.CLIENT_ID_CONFIG, "funKafkaProduce")
                            set(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
                            set(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
                            set(ProducerConfig.ACKS_CONFIG, "all")
                            set(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1)
                            set(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 500)
                            set(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
                            set(SaslConfigs.SASL_MECHANISM, "PLAIN")
                            set(SaslConfigs.SASL_JAAS_CONFIG, "$JAAS_PLAIN_LOGIN $JAAS_REQUIRED username=\"$user\" password=\"$pwd\";")
                        }
                ).use { p ->
                    withTimeoutOrNull(10_000) {
                        data.forEach { k, v -> p.send(ProducerRecord(topic, k, v)).get() }
                        true
                    } ?: false
                }
            } catch (e: Exception) {
                false
            }

}
