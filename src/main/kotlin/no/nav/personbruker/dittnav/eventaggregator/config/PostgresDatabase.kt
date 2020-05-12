package no.nav.personbruker.dittnav.eventaggregator.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.health.HealthStatus
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PostgresDatabase(env: Environment) : Database {

    private val envDataSource: HikariDataSource
    private val log: Logger = LoggerFactory.getLogger(PostgresDatabase::class.java)

    init {
        envDataSource = createCorrectConnectionForEnvironment(env)
    }

    override val dataSource: HikariDataSource
        get() = envDataSource

    override suspend fun status(): HealthStatus {
        val serviceName = "Database"
        return withContext(Dispatchers.IO) {
            try {
                dbQuery { prepareStatement("""SELECT 1""").execute() }
                HealthStatus(serviceName, Status.OK, "200 OK")
            } catch (e: Exception) {
                log.error("Selftest mot databasen feilet", e)
                HealthStatus(serviceName, Status.ERROR, "Feil mot DB")
            }}
    }

    private fun createCorrectConnectionForEnvironment(env: Environment): HikariDataSource {
        return when (ConfigUtil.isCurrentlyRunningOnNais()) {
            true -> createConnectionViaVaultWithDbUser(env)
            false -> createConnectionForLocalDbWithDbUser(env)
        }
    }

    private fun createConnectionForLocalDbWithDbUser(env: Environment): HikariDataSource {
        return hikariFromLocalDb(env, env.dbUser)
    }

    private fun createConnectionViaVaultWithDbUser(env: Environment): HikariDataSource {
        return hikariDatasourceViaVault(env, env.dbUser)
    }

    companion object {

        fun hikariFromLocalDb(env: Environment, dbUser: String): HikariDataSource {
            val dbPassword: String = getEnvVar("DB_PASSWORD")
            val config = hikariCommonConfig(env).apply {
                username = dbUser
                password = dbPassword
                validate()
            }
            return HikariDataSource(config)
        }

        fun hikariDatasourceViaVault(env: Environment, dbUser: String): HikariDataSource {
            val config = hikariCommonConfig(env)
            config.validate()
            return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, env.dbMountPath, dbUser)
        }

        private fun hikariCommonConfig(env: Environment): HikariConfig {
            val config = HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = env.dbUrl
                minimumIdle = 0
                maxLifetime = 30001
                maximumPoolSize = 5
                connectionTimeout = 1000
                validationTimeout = 250
                idleTimeout = 10001
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            }
            return config
        }
    }

}
