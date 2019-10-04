package no.nav.personbruker.dittnav.eventaggregator.database

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

interface Database {

    val dataSource: HikariDataSource

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
