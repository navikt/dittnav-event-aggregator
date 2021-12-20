package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.KafkaTestTopics
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.SecurityConfig
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
                withSecurity = false,
                withSchemaRegistry = true,
                users = listOf(JAASCredential(username, password))
        )
    }

    fun createEnvironmentForEmbeddedKafka(embeddedEnv: KafkaEnvironment): Environment {
        return Environment(
                username = username,
                password = password,
                groupId = "groupId-for-tests",
                dbHost = "dbHostIkkeIBrukHer",
                dbPort = "dbPortIkkeIBrukHer",
                dbName = "dbNameIkkeIBrukHer",
                dbUrl = "dbUrlIkkeIBrukHer",
                dbUser = "dbUserIkkeIBrukHer",
                dbPassword = "dbPWIkkeIBrukHer",
                clusterName = "clusterNameIkkeIBrukHer",
                namespace = "namespaceIkkeIBrukHer",
                appnavn = "test-app",
                influxdbHost = "",
                influxdbPort = 0,
                influxdbName = "",
                influxdbUser = "",
                influxdbPassword = "",
                influxdbRetentionPolicy = "",
                aivenBrokers = embeddedEnv.brokersURL.substringAfterLast("/"),
                aivenSchemaRegistry = embeddedEnv.schemaRegistry!!.url,
                securityConfig = SecurityConfig(enabled = false),
                beskjedInternTopicName = KafkaTestTopics.beskjedInternTopicName,
                oppgaveInternTopicName = KafkaTestTopics.oppgaveInternTopicName,
                innboksInternTopicName = KafkaTestTopics.innboksInternTopicName,
                statusoppdateringInternTopicName = KafkaTestTopics.statusoppdateringInternTopicName,
                doneInternTopicName = KafkaTestTopics.doneInternTopicName,
                doneInputTopicName = KafkaTestTopics.doneInputTopicName
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
