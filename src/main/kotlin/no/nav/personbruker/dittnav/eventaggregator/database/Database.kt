package no.nav.personbruker.dittnav.eventaggregator.database

import java.sql.Connection
import javax.sql.DataSource

interface Database {

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
