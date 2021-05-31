package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.common.util.config.IntEnvVar.getEnvVarAsInt
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar

data class Environment(val username: String = getEnvVar("SERVICEUSER_USERNAME"),
                       val password: String = getEnvVar("SERVICEUSER_PASSWORD"),
                       val groupId: String = getEnvVar("GROUP_ID"),
                       val dbHost: String = getEnvVar("DB_HOST"),
                       val dbName: String = getEnvVar("DB_NAME"),
                       val dbAdmin: String = getEnvVar("DB_NAME") + "-admin",
                       val dbUser: String = getEnvVar("DB_NAME") + "-user",
                       val dbUrl: String = "jdbc:postgresql://$dbHost/$dbName",
                       val dbMountPath: String = getEnvVar("DB_MOUNT_PATH"),
                       val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
                       val namespace: String = getEnvVar("NAIS_NAMESPACE"),
                       val influxdbHost: String = getEnvVar("INFLUXDB_HOST"),
                       val influxdbPort: Int = getEnvVarAsInt("INFLUXDB_PORT"),
                       val influxdbName: String = getEnvVar("INFLUXDB_DATABASE_NAME"),
                       val influxdbUser: String = getEnvVar("INFLUXDB_USER"),
                       val influxdbPassword: String = getEnvVar("INFLUXDB_PASSWORD"),
                       val influxdbRetentionPolicy: String = getEnvVar("INFLUXDB_RETENTION_POLICY"),
                       val aivenBrokers: String = getEnvVar("KAFKA_BROKERS"),
                       val aivenTruststorePath: String = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
                       val aivenKeystorePath: String = getEnvVar("KAFKA_KEYSTORE_PATH"),
                       val aivenCredstorePassword: String = getEnvVar("KAFKA_CREDSTORE_PASSWORD"),
                       val aivenSchemaRegistry: String = getEnvVar("KAFKA_SCHEMA_REGISTRY"),
                       val aivenSchemaRegistryUser: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_USER"),
                       val aivenSchemaRegistryPassword: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_PASSWORD")
)

fun isOtherEnvironmentThanProd() = System.getenv("NAIS_CLUSTER_NAME") != "prod-sbs"

fun isProdEnvironment() = System.getenv("NAIS_CLUSTER_NAME") == "prod-sbs"
