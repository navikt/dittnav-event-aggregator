package no.nav.personbruker.dittnav.eventaggregator.common.database

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.done.deleteAllDone
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.statusoppdatering.deleteAllStatusoppdatering
import org.flywaydb.core.Flyway

class LocalPostgresDatabase private constructor() : Database {

    private val memDataSource: HikariDataSource
    private val container = TestPostgresqlContainer()

    companion object {
        private val instance by lazy {
            LocalPostgresDatabase().also {
                it.migrate()
            }
        }

        fun migratedDb(): LocalPostgresDatabase {
            runBlocking {
                //En del raskere å slette alle radene enn å kjøre clean/migrate hver gang
                instance.dbQuery {
                    deleteAllStatusoppdatering()
                    deleteAllDone()
                    deleteAllBeskjed()
                    deleteAllOppgave()
                    deleteAllInnboks()
                }
            }
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
}
