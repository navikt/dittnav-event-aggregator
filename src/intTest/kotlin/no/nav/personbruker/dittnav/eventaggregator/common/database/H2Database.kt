package no.nav.personbruker.dittnav.eventaggregator.common.database

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.personbruker.dittnav.eventaggregator.health.HealthStatus
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class H2Database : Database {

    private val memDataSource: HikariDataSource
    private val log: Logger = LoggerFactory.getLogger(H2Database::class.java)

    init {
        memDataSource = createDataSource()
        flyway()
    }

    override val dataSource: HikariDataSource
        get() = memDataSource

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

    private fun createDataSource(): HikariDataSource {
        return HikariDataSource().apply {
            jdbcUrl = "jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
            username = "sa"
            password = ""
            validate()
        }
    }

    private fun flyway() {
        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate()
    }

}
