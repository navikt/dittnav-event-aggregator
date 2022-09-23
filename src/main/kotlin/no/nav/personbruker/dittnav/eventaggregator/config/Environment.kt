package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.common.util.config.BooleanEnvVar.getEnvVarAsBoolean
import no.nav.personbruker.dittnav.common.util.config.IntEnvVar.getEnvVarAsInt
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar
import no.nav.personbruker.dittnav.eventaggregator.config.ConfigUtil.isCurrentlyRunningOnNais

data class Environment(val groupId: String = getEnvVar("GROUP_ID"),
                       val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
                       val namespace: String = getEnvVar("NAIS_NAMESPACE"),
                       val influxdbHost: String = getEnvVar("INFLUXDB_HOST"),
                       val influxdbPort: Int = getEnvVarAsInt("INFLUXDB_PORT"),
                       val influxdbName: String = getEnvVar("INFLUXDB_DATABASE_NAME"),
                       val influxdbUser: String = getEnvVar("INFLUXDB_USER"),
                       val influxdbPassword: String = getEnvVar("INFLUXDB_PASSWORD"),
                       val influxdbRetentionPolicy: String = getEnvVar("INFLUXDB_RETENTION_POLICY"),
                       val aivenBrokers: String = getEnvVar("KAFKA_BROKERS"),
                       val aivenSchemaRegistry: String = getEnvVar("KAFKA_SCHEMA_REGISTRY"),
                       val securityConfig: SecurityConfig = SecurityConfig(isCurrentlyRunningOnNais()),
                       val dbUser: String = getEnvVar("DB_USERNAME"),
                       val dbPassword: String = getEnvVar("DB_PASSWORD"),
                       val dbHost: String = getEnvVar("DB_HOST"),
                       val dbPort: String = getEnvVar("DB_PORT"),
                       val dbName: String = getEnvVar("DB_DATABASE"),
                       val dbUrl: String = getDbUrl(dbHost, dbPort, dbName),
                       val beskjedInternTopicName: String = getEnvVar("INTERN_BESKJED_TOPIC"),
                       val oppgaveInternTopicName: String = getEnvVar("INTERN_OPPGAVE_TOPIC"),
                       val innboksInternTopicName: String = getEnvVar("INTERN_INNBOKS_TOPIC"),
                       val doneInternTopicName: String = getEnvVar("INTERN_DONE_TOPIC"),
                       val doneInputTopicName: String = getEnvVar("INPUT_DONE_TOPIC"),
                       val doknotifikasjonStatusGroupId: String = getEnvVar("GROUP_ID_DOKNOTIFIKASJON_STATUS"),
                       val doknotifikasjonStatusTopicName: String = getEnvVar("DOKNOTIFIKASJON_STATUS_TOPIC"),
                       val rapidTopic: String = getEnvVar("RAPID_TOPIC"),
                       val rapidEnabled: Boolean = getEnvVar("RAPID_ENABLED", "false").toBoolean(),
                       val rapidWriteToDb: Boolean = getEnvVar("RAPID_WRITE_TO_DB", "false").toBoolean(),
                       val archivingEnabled: Boolean = getEnvVarAsBoolean("ARCHIVING_ENABLED"),
                       val archivingThresholdDays: Int = getEnvVarAsInt("ARCHIVING_THRESHOLD")
) {
    fun rapidConfig(): Map<String, String> = mapOf(
        "KAFKA_BROKERS" to aivenBrokers,
        "KAFKA_CONSUMER_GROUP_ID" to "dittnav-event-aggregator-v1",
        "KAFKA_RAPID_TOPIC" to rapidTopic,
        "KAFKA_KEYSTORE_PATH" to securityConfig.variables!!.aivenKeystorePath,
        "KAFKA_CREDSTORE_PASSWORD" to securityConfig.variables.aivenCredstorePassword,
        "KAFKA_TRUSTSTORE_PATH" to securityConfig.variables.aivenTruststorePath,
        "HTTP_PORT" to "8090"
    )
}

data class SecurityConfig(
        val enabled: Boolean,

        val variables: SecurityVars? = if (enabled) {
            SecurityVars()
        } else {
            null
        }
)

data class SecurityVars(
        val aivenTruststorePath: String = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
        val aivenKeystorePath: String = getEnvVar("KAFKA_KEYSTORE_PATH"),
        val aivenCredstorePassword: String = getEnvVar("KAFKA_CREDSTORE_PASSWORD"),
        val aivenSchemaRegistryUser: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_USER"),
        val aivenSchemaRegistryPassword: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_PASSWORD")
)

fun isOtherEnvironmentThanProd() = System.getenv("NAIS_CLUSTER_NAME") != "prod-sbs"

fun isProdEnvironment() = System.getenv("NAIS_CLUSTER_NAME") == "prod-sbs"

fun shouldPollBeskjed() = StringEnvVar.getOptionalEnvVar("POLL_BESKJED", "true").toBoolean()

fun shouldPollOppgave() = StringEnvVar.getOptionalEnvVar("POLL_OPPGAVE", "true").toBoolean()

fun shouldPollInnboks() = StringEnvVar.getOptionalEnvVar("POLL_INNBOKS", "true").toBoolean()

fun shouldPollDone() = StringEnvVar.getOptionalEnvVar("POLL_DONE", "true").toBoolean()

fun shouldPollDoknotifikasjonStatus() = StringEnvVar.getOptionalEnvVar("POLL_DOKNOTIFIKASJON_STATUS", "false").toBoolean()

fun getDbUrl(host: String, port: String, name: String): String {
    return if (host.endsWith(":$port")) {
        "jdbc:postgresql://${host}/$name"
    } else {
        "jdbc:postgresql://${host}:${port}/${name}"
    }
}
