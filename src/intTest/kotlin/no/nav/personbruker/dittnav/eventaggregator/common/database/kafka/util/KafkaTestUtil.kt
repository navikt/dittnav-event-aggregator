package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import org.apache.avro.generic.GenericRecord

object KafkaTestUtil {

    val username = "srvkafkaclient"
    val password = "kafkaclient"

    fun createDefaultKafkaEmbeddedInstance(topics: List<String>): KafkaEnvironment {
        return KafkaEnvironment(
                topicNames = topics,
                withSecurity = false,
                withSchemaRegistry = true,
                users = listOf(JAASCredential(username, password))
        )
    }

    fun createKafkaEmbeddedInstanceWithNumPartitions(topics: List<String>, partitions: Int): KafkaEnvironment {
        val topicInfos = topics.map { KafkaEnvironment.TopicInfo(it, partitions = partitions) }

        return KafkaEnvironment(
                topicInfos = topicInfos,
                withSecurity = true,
                withSchemaRegistry = true,
                users = listOf(JAASCredential(username, password))
        )
    }

    fun createEnvironmentForEmbeddedKafka(embeddedEnv: KafkaEnvironment): Environment {
        return Environment(
                username = username,
                password = password,
                groupId = "groupId-for-tests",
                dbAdmin = "dbAdminIkkeIBrukHer",
                dbHost = "dbHostIkkeIBrukHer",
                dbMountPath = "dbMountPathIkkeIBrukHer",
                dbName = "dbNameIkkeIBrukHer",
                dbUrl = "dbUrlIkkeIBrukHer",
                dbUser = "dbUserIkkeIBrukHer",
                clusterName = "clusterNameIkkeIBrukHer",
                namespace = "namespaceIkkeIBrukHer",
                sensuHost = "sensuHostIkkeIBrukHer",
                sensuPort = 0,
                aivenBrokers = embeddedEnv.brokersURL.substringAfterLast("/"),
                aivenTruststorePath = "kafkaTruststorePathIkkeIBrukHer",
                aivenKeystorePath = "kafkaKeystorePathIkkeIBrukerHer",
                aivenCredstorePassword = "kafkaCredstorePasswordIkkeIBrukHer",
                aivenSchemaRegistry = embeddedEnv.schemaRegistry!!.url,
                aivenSchemaRegistryUser = username,
                aivenSchemaRegistryPassword = password
        )
    }

    suspend fun produceEvents(env: Environment, topicName: String, events: Map<NokkelIntern, GenericRecord>): Boolean {
        return KafkaProducerUtil.kafkaAvroProduce(
                env.aivenBrokers,
                env.aivenSchemaRegistry,
                topicName,
                events)
    }

}
