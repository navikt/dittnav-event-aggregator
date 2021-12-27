package no.nav.personbruker.dittnav.eventaggregator.common.database

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

class LocalPostgresDatabase private constructor() : Database {

    private val memDataSource: HikariDataSource
    private val container = TestPostgresqlContainer()

    companion object {
        private val instance by lazy {
            LocalPostgresDatabase()
        }

        fun migratedDb(): LocalPostgresDatabase {
            instance.clean()
            instance.migrate()
            return instance
        }
    }

    init {
        container.start()
        memDataSource = createDataSource()
    }

    override val dataSource: HikariDataSource
        get() = memDataSource

    private fun createDataSource(): HikariDataSource {
        return HikariDataSource().apply {
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            isAutoCommit = false
            validate()
        }
    }

    private fun migrate() {
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()
    }

    private fun clean() = Flyway.configure().dataSource(dataSource).load().clean()
}
