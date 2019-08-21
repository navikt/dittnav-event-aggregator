package no.nav.personbruker.dittnav.eventaggregator.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import java.lang.Exception
import java.sql.Connection
import javax.sql.DataSource

class Database(env: Environment) {

    private val dataSource: DataSource

    init {
        dataSource = createCorrectConnectionForEnvironment(env)
    }

    fun <T> dbQuery(block: Connection.() -> T): T =
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
            val config = hikariCommonConfig(env)
            config.username = dbUser
            config.password = env.dbPassword
            config.validate()
            return HikariDataSource(config)
        }

        fun hikariDatasourceViaVault(env: Environment, dbUser: String): HikariDataSource {
            var config = hikariCommonConfig(env)
            config.validate()
            return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, env.dbMountPath, dbUser)
        }

        private fun hikariCommonConfig(env: Environment): HikariConfig {
            val config = HikariConfig()
            config.driverClassName = "org.postgresql.Driver"
            config.jdbcUrl = env.dbUrl
            config.minimumIdle = 0
            config.maxLifetime = 30001
            config.maximumPoolSize = 2
            config.connectionTimeout = 250
            config.idleTimeout = 10001
            config.isAutoCommit = false
            config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            return config
        }
    }

}


