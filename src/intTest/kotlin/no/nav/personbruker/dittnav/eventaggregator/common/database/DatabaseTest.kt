package no.nav.personbruker.dittnav.eventaggregator.common.database

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.sql.SQLTransientException

class DatabaseTest {

    private val database = H2Database()

    @Test
    fun `Skal ikke gjore noe hvis det ikke blir kastet en exception`() {
        var denneVariablelenSkalHaBlittFlippet = false
        database.translateExternalExceptionsToInternalOnes {
            denneVariablelenSkalHaBlittFlippet = true
        }
        denneVariablelenSkalHaBlittFlippet `should be` true
    }

    @Test
    fun `Skal haandtere ukjente exceptions, og mappe til intern exceptiontype`() {
        invoking {
            database.translateExternalExceptionsToInternalOnes {
                throw Exception("Simulert exception")
            }
        } `should throw` UnretriableDatabaseException::class
    }

    @Test
    fun `Skal haandtere SQLException, og mappe til intern exceptiontype`() {
        invoking {
            database.translateExternalExceptionsToInternalOnes {
                throw SQLException("Simulert exception")
            }
        } `should throw` UnretriableDatabaseException::class
    }

    @Test
    fun `Skal haandtere SQLTransientException, og mappe til intern exceptiontype`() {
        invoking {
            database.translateExternalExceptionsToInternalOnes {
                throw SQLTransientException("Simulert exception")
            }
        } `should throw` RetriableDatabaseException::class
    }

    @Test
    fun `Skal haandtere SQLRecoverableException, og mappe til intern exceptiontype`() {
        invoking {
            database.translateExternalExceptionsToInternalOnes {
                throw SQLTransientException("Simulert exception")
            }
        } `should throw` RetriableDatabaseException::class
    }

}
