package no.nav.personbruker.dittnav.eventaggregator.common.database

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import java.sql.*

interface Database {

    val dataSource: HikariDataSource

    suspend fun <T> dbQuery(operationToExecute: Connection.() -> T): T =
            dataSource.connection.use { openConnection ->
                try {
                    openConnection.operationToExecute().apply {
                        openConnection.commit()
                    }

                } catch (e: Exception) {
                    try {
                        openConnection.rollback()
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
            val message = "Det skjedde en ukjent feil ved skriving til databasen."
            throw UnretriableDatabaseException(message, e)
        }
    }

    fun queryWithExceptionTranslation(operationToExecute: Connection.() -> Unit) {
        translateExternalExceptionsToInternalOnes {
            runBlocking {
                dbQuery {
                    operationToExecute()
                }
            }
        }
    }
}
