package no.nav.personbruker.dittnav.eventaggregator.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.personbruker.dittnav.eventaggregator.config.ConfigUtil
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import java.lang.Exception
import java.sql.Connection
import javax.sql.DataSource

interface IDatabase {

    val dataSource: DataSource

    suspend fun <T> dbQuery(block: Connection.() -> T): T =
            dataSource.connection.use {
                try {
                    it.block().apply { it.commit() }
                } catch (e: Exception) {
                    try {
                        it.rollback()
                    } catch (rollbackException: Exception) {
                        e.addSuppressed(rollbackException)
                    }
                    throw e
                }
            }
}

class Database(env: Environment) : IDatabase {

    private val envDataSource: DataSource

    init {
        envDataSource = createCorrectConnectionForEnvironment(env)
    }

    override val dataSource: DataSource
        get() = envDataSource


    private fun createCorrectConnectionForEnvironment(env: Environment) : HikariDataSource {
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
            val config = hikariCommonConfig(env).apply {
                username = dbUser
                password = env.dbPassword
                validate()
            }
            return HikariDataSource(config)
        }

        fun hikariDatasourceViaVault(env: Environment, dbUser: String): HikariDataSource {
            var config = hikariCommonConfig(env)
            config.validate()
            return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, env.dbMountPath, dbUser)
        }

        private fun hikariCommonConfig(env: Environment): HikariConfig {
            val config = HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = env.dbUrl
                minimumIdle = 0
                maxLifetime = 30001
                maximumPoolSize = 2
                connectionTimeout = 250
                idleTimeout = 10001
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            }
            return config
        }
    }

}


