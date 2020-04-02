package no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import org.amshove.kluent.`should be null`
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class SwallowSerializationErrorsAvroDeserializerTest {

    private val topic = "dummyTopic"
    private val config = mutableMapOf<String, Any>()
    private val schemaRegistryClient: SchemaRegistryClient
    private val serializer: KafkaAvroSerializer
    private val deserializer: SwallowSerializationErrorsAvroDeserializer

    init {
        config[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "thisUrlMustBeSetAtLeastToADummyValue"
        config[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true

        schemaRegistryClient = MockSchemaRegistryClient()
        serializer = KafkaAvroSerializer(schemaRegistryClient, config)
        deserializer = SwallowSerializationErrorsAvroDeserializer(schemaRegistryClient, config)
    }

    @Test
    fun `should serialize valid records successfully`() {
        val original = AvroBeskjedObjectMother.createBeskjed(1)
        val serialized = serializer.serialize(topic, original)
        val deserialized = deserializer.deserialize(topic, serialized)

        deserialized shouldEqual original
    }

    @Test
    fun `should return null for invalid records`() {
        val invalidSerialisedEvent = ByteArray(10)

        val deserialiedRecord = deserializer.deserialize(topic, invalidSerialisedEvent)

        deserialiedRecord.`should be null`()
    }

}
