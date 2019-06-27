package no.nav.personbruker.dittnav.eventaggregator.config

data class Environment(val bootstrapServers: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
                       val schemaRegistryUrl: String = getEnvVar("KAFKA_SCHEMAREGISTRY_SERVERS", "http://localhost:8081"),
                       val username: String = getEnvVar("FSS_SYSTEMUSER_USERNAME", "username"),
                       val password: String = getEnvVar("FSS_SYSTEMUSER_PASSWORD", "password"),
                       val groupId: String = getEnvVar("GROUP_ID", "dittnav_events"),
                       val dbUrl: String = getEnvVar("DB_URL", "jdbc:postgresql://localhost:5432/dittnav-event-cache"),
                       val dbUser: String = getEnvVar("DB_USER", "testuser"),
                       val dbPassword: String = getEnvVar("DB_PASSWORD", "testpassword"),
                       val dbMountPath: String = getEnvVar("DB_MOUNT_PATH", "notUsedOnLocalhost")
)

fun getEnvVar(varName: String, defaultValue: String? = null): String {
    return System.getenv(varName) ?: defaultValue
    ?: throw IllegalArgumentException("Variable $varName cannot be empty")
}
