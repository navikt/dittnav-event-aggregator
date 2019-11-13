package no.nav.personbruker.dittnav.eventaggregator.melding

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test


class MeldingQueriesTest {
    private val database = H2Database()

    private val aktorId1 = "12345"
    private val aktorId2 = "67890"

    private val melding1: Melding
    private val melding2: Melding
    private val melding3: Melding

    private val allMelding: List<Melding>
    private val allMeldingForAktor1: List<Melding>

    init {
        melding1 = createMelding("1", aktorId1)
        melding2 = createMelding("2", aktorId2)
        melding3 = createMelding("3", aktorId1)

        allMelding = listOf(melding1, melding2, melding3)
        allMeldingForAktor1 = listOf(melding1, melding3)
    }

    private fun createMelding(eventId: String, aktorId: String): Melding {
        val melding = MeldingObjectMother.createMelding(eventId, aktorId)

        return runBlocking {
            database.dbQuery {
                createMelding(melding)
                        .let { melding.copy(id = it) }
            }
        }
    }

    @AfterAll
    fun cleanUp() {
        runBlocking {
            database.dbQuery {
                deleteAllMelding()
            }
        }
    }

    @Test
    fun `Should find all Melding`() {
        runBlocking {
            database.dbQuery {
                val result = getAllMelding()
                result.size `should be equal to` allMelding.size
                result `should contain all` allMelding
            }
        }
    }

    @Test
    fun `should find Melding by id`() {
        runBlocking {
            database.dbQuery {
                val result = getMeldingById(melding1.id!!)
                result `should equal` melding1
            }
        }
    }

    @Test
    fun `should set aktiv flag`() {
        runBlocking {
            database.dbQuery {
                setMeldingAktivFlag("1", false)
                var melding = getMeldingByEventId("1")
                melding.aktiv `should be equal to` false

                setMeldingAktivFlag("1", true)
                melding = getMeldingByEventId("1")
                melding.aktiv `should be equal to` true
            }
        }
    }

    @Test
    fun `should find Melding by aktiv flag`() {
        runBlocking {
            database.dbQuery {
                setMeldingAktivFlag(melding1.eventId, false)
                val aktiveMelding = getAllMeldingByAktiv(true)
                val inaktivMelding = getAllMeldingByAktiv(false)

                aktiveMelding.none { it.id == melding1.id }
                aktiveMelding.size `should be equal to` allMelding.size - 1
                inaktivMelding.single { it.id == melding1.id }
                inaktivMelding.size `should be equal to` 1

                setMeldingAktivFlag(melding1.eventId, true)
            }
        }
    }

    @Test
    fun `should find Melding by aktorId`() {
        runBlocking {
            database.dbQuery {
                val result = getMeldingByAktorId(aktorId1)
                result.size `should be equal to` allMeldingForAktor1.size
                result `should contain all` allMeldingForAktor1
                result `should not contain` melding2
            }
        }
    }

    @Test
    fun `should find Melding by eventId`() {
        runBlocking {
            database.dbQuery {
                val result = getMeldingByEventId(melding1.eventId)
                result `should equal` melding1
            }
        }
    }
}