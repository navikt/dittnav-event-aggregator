package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


internal class BeskjedRepositoryTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val beskjedRepository = BeskjedRepository(database)
    private val fnr = "123456789081"

    private val aktivBeskjed = BeskjedTestData.aktivBeskjed(eventId = "987653", fodselsnummer = fnr)
    private val inaktivBeskjed = BeskjedTestData.inaktivBeskjed(eventId = "98767777", fodselsnummer = fnr)

    @BeforeEach
    fun populate() {
        runBlocking {
            database.dbQuery {
                createBeskjed(aktivBeskjed)
                createBeskjed(inaktivBeskjed)
            }
        }
    }

    @AfterEach
    fun cleanup() {
        runBlocking {
            database.dbQuery {
                deleteAllBeskjed()
            }
        }
    }


    @Test
    fun `inaktiverer aktivt beskjedvarsel`() {
        beskjedRepository.setBeskjedInactive(aktivBeskjed.eventId) shouldBe 1
        runBlocking {
            database.dbQuery {
                getAllBeskjedByAktiv(false).apply {
                    size shouldBe 2
                    filter { it.eventId == aktivBeskjed.eventId }.size shouldBe 1
                }
            }
        }

    }

    @Test
    fun `gjør ingenting om varselet allerede en innaktivert`() {
        beskjedRepository.setBeskjedInactive(inaktivBeskjed.eventId) shouldBe 0
        runBlocking {
            database.dbQuery {
                getAllBeskjedByAktiv(false).apply {
                    size shouldBe 1
                    filter { it.eventId == inaktivBeskjed.eventId }.size shouldBe 1
                }
            }
        }
    }

    @Test
    fun `kaster excpetion hvis beskjedvarselet ikke finnes`() {
        assertThrows<BeskjedNotFoundException> {
            beskjedRepository.setBeskjedInactive("even3tfinnesikke")
        }

    }
}