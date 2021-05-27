package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.*

object KafkaConsumerUtil {

    suspend fun kafkaAvroConsume(
            brokersURL: String,
            schemaRegistryUrl: String,
            topic: String,
            noOfEvents: Int
    ): Map<NokkelIntern, String> =
            try {

                KafkaConsumer<NokkelIntern, String>(
                        Properties().apply {
                            set(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersURL)
                            set(ConsumerConfig.CLIENT_ID_CONFIG, "funKafkaAvroConsume")
                            set(ConsumerConfig.GROUP_ID_CONFIG, "funKafkaAvroConsumeGrpID")
                            set(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer")
                            set(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer")
                            set(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)
                            set(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
                            set(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true)
                            set(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                            set(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 4)
                        }
                ).use { c ->
                    c.subscribe(listOf(topic))

                    val fE = mutableMapOf<NokkelIntern, String>()

                    withTimeoutOrNull(10_000) {

                        while (fE.size < noOfEvents) {
                            delay(100)
                            c.poll(Duration.ofSeconds(50)).forEach { e -> fE[e.key()] = e.value() }
                        }
                        fE
                    } ?: emptyMap()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

    suspend fun kafkaConsume(
            brokersURL: String,
            topic: String,
            noOfEvents: Int
    ): Map<String, String> =
            try {
                KafkaConsumer<String, String>(
                        Properties().apply {
                            set(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersURL)
                            set(ConsumerConfig.CLIENT_ID_CONFIG, "funKafkaConsume")
                            set(ConsumerConfig.GROUP_ID_CONFIG, "funKafkaConsumeGrpID")
                            set(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
                            set(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
                            set(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true)
                            set(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                            set(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 4)
                        }
                ).use { c ->
                    c.subscribe(listOf(topic))

                    val fE = mutableMapOf<String, String>()

                    withTimeoutOrNull(10_000) {

                        while (fE.size < noOfEvents) {
                            delay(100)
                            c.poll(Duration.ofMillis(500)).forEach { e -> fE[e.key()] = e.value() }
                        }
                        fE
                    } ?: emptyMap()
                }
            } catch (e: Exception) {
                emptyMap()
            }

}
