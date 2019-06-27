package no.nav.personbruker.dittnav.eventaggregator.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object DatabaseConnectionFactory {

    fun initDatabase(env: Environment) {
        Database.connect(createCorrectDatasourceForEnvironment(env))
    }

    fun createCorrectDatasourceForEnvironment(env: Environment): DataSource {
        if (ConfigUtil.isCurrentlyRunningOnNais()) {
            return hikariDatasourceViaVault(env)

        } else {
            return hikariFromLocalDb(env)
        }
    }

    private fun hikariDatasourceViaVault(env: Environment): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = env.dbUrl
        config.minimumIdle = 0
        config.maxLifetime = 30001
        config.maximumPoolSize = 2
        config.connectionTimeout = 250
        config.idleTimeout = 10001
        // TODO: Kun bruke dittnav-event-cache-preprod-admin for Flyway, og heller bruke dittnav-event-cache-preprod-user ellers
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, env.dbMountPath, env.dbAdmin)
    }

    private fun hikariFromLocalDb(env: Environment): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = env.dbUrl
        config.username = env.dbAdmin
        config.password = env.dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction { block() }
            }

}
