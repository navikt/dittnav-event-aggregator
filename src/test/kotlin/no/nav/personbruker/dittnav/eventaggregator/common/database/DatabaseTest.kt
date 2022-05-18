package no.nav.personbruker.dittnav.eventaggregator.common.database

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.AggregatorBatchUpdateException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import java.sql.BatchUpdateException
import java.sql.SQLException
import java.sql.SQLTransientException

class DatabaseTest {

    @Test
    fun `Skal ikke gjore noe hvis det ikke blir kastet en exception`() {
        var denneVariablelenSkalHaBlittFlippet = false
        translateExternalExceptionsToInternalOnes {
            denneVariablelenSkalHaBlittFlippet = true
        }
        denneVariablelenSkalHaBlittFlippet shouldBe true
    }

    @Test
    fun `Skal haandtere ukjente exceptions, og mappe til intern exceptiontype`() {
        shouldThrow<UnretriableDatabaseException> {
            translateExternalExceptionsToInternalOnes {
                throw Exception("Simulert exception")
            }
        }
    }

    @Test
    fun `Skal haandtere SQLException, og mappe til intern exceptiontype`() {
        shouldThrow<UnretriableDatabaseException> {
            translateExternalExceptionsToInternalOnes {
                throw SQLException("Simulert exception")
            }
        }
    }

    @Test
    fun `Skal haandtere PSQLException, og mappe til intern exceptiontype`() {
        shouldThrow<UnretriableDatabaseException> {
            translateExternalExceptionsToInternalOnes {
                throw PSQLException("Simulert exception", PSQLState.COMMUNICATION_ERROR)
            }
        }
    }

    @Test
    fun `Skal haandtere SQLTransientException, og mappe til intern exceptiontype`() {
        shouldThrow<RetriableDatabaseException> {
            translateExternalExceptionsToInternalOnes {
                throw SQLTransientException("Simulert exception")
            }
        }
    }

    @Test
    fun `Skal haandtere SQLRecoverableException, og mappe til intern exceptiontype`() {
        shouldThrow<RetriableDatabaseException> {
            translateExternalExceptionsToInternalOnes {
                throw SQLTransientException("Simulert exception")
            }
        }
    }

    @Test
    fun `Skal haandtere BatchUpdateException, og mappe til intern exceptiontype`() {
        shouldThrow<AggregatorBatchUpdateException > {
            translateExternalExceptionsToInternalOnes {
                throw BatchUpdateException("Simulert exception", IntArray(1))
            }
        }
    }

}
