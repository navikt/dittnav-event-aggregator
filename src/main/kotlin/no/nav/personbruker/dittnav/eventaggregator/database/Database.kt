package no.nav.personbruker.dittnav.eventaggregator.database

import com.zaxxer.hikari.HikariDataSource
import no.nav.personbruker.dittnav.eventaggregator.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.exceptions.UnretriableDatabaseException
import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLRecoverableException
import java.sql.SQLTransientException

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

    fun translateExternalExceptionsToInternalOnes(databaseActions: () -> Unit) {
        try {
            databaseActions()

        } catch (te: SQLTransientException) {
            val message = "Skriving til databasen feilet grunnet en periodisk feil."
            throw RetriableDatabaseException(message, te)

        } catch (re: SQLRecoverableException) {
            val message = "Skriving til databasen feilet grunnet en periodisk feil."
            throw RetriableDatabaseException(message, re)

        } catch (se: SQLException) {
            val message = "Det skjedde en SQL relatert feil ved skriving til databasen."
            throw UnretriableDatabaseException(message, se)

        } catch (e: Exception) {
            // Stop alle polle-jobber (disse vil starte igjen nå databasen er tilgjengelig igjen)
            val message = "Det skjedde en ukjent feil ved skriving til databasen."
            throw UnretriableDatabaseException(message, e)
        }
    }

}
