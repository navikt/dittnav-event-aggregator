package no.nav.personbruker.dittnav.eventaggregator.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConnectionFactory {

//    private val appConfig = HoconApplicationConfig(ConfigFactory.load())
    private val dbUrl =  "jdbc:postgresql://localhost:5432/dittnav-event-cache" //appConfig.property("db.jdbcUrl").getString()
    private val dbUser = "testuser" // appConfig.property("db.dbUser").getString()
    private val dbPassword = "testpassword" // appConfig.property("db.dbPassword").getString()

    fun runFlywayMigrations(): Database {
        val connection = Database.connect(hikariFromLocalDb())
        val flyway = Flyway.configure().dataSource(dbUrl, dbUser, dbPassword).load()
        flyway.migrate()
        return connection
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

    private fun hikariFromLocalDb(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password = dbPassword
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
