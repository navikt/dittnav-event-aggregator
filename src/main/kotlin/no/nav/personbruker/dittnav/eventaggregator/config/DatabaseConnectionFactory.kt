package no.nav.personbruker.dittnav.eventaggregator.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConnectionFactory {

    fun initDatabase(env: Environment) {
        Database.connect(hikariFromLocalDb(env))
    }

    fun hikariDatasourceViaVault(): HikariDataSource? {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/testdb"
        config.minimumIdle = 0
        config.maxLifetime = 30001
        config.maximumPoolSize = 2
        config.connectionTimeout = 250
        config.idleTimeout = 10001
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, "postgresql/preprod", "testdb-user")
    }

    fun hikariFromLocalDb(env: Environment): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = env.dbUrl
        config.username = env.dbUser
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
