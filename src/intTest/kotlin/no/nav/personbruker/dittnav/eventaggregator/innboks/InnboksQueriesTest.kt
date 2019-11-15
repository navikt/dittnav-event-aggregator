package no.nav.personbruker.dittnav.eventaggregator.innboks

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test


class InnboksQueriesTest {
    private val database = H2Database()

    private val aktorId1 = "12345"
    private val aktorId2 = "67890"

    private val innboks1: Innboks
    private val innboks2: Innboks
    private val innboks3: Innboks

    private val allInnboks: List<Innboks>
    private val allInnboksForAktor1: List<Innboks>

    init {
        innboks1 = createInnboks("1", aktorId1)
        innboks2 = createInnboks("2", aktorId2)
        innboks3 = createInnboks("3", aktorId1)

        allInnboks = listOf(innboks1, innboks2, innboks3)
        allInnboksForAktor1 = listOf(innboks1, innboks3)
    }

    private fun createInnboks(eventId: String, aktorId: String): Innboks {
        val innboks = InnboksObjectMother.createInnboks(eventId, aktorId)

        return runBlocking {
            database.dbQuery {
                createInnboks(innboks)
                        .let { innboks.copy(id = it) }
            }
        }
    }

    @AfterAll
    fun cleanUp() {
        runBlocking {
            database.dbQuery {
                deleteAllInnboks()
            }
        }
    }

    @Test
    fun `Should find all Innboks`() {
        runBlocking {
            database.dbQuery {
                val result = getAllInnboks()
                result.size `should be equal to` allInnboks.size
                result `should contain all` allInnboks
            }
        }
    }

    @Test
    fun `should find Innboks by id`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksById(innboks1.id!!)
                result `should equal` innboks1
            }
        }
    }

    @Test
    fun `should set aktiv flag`() {
        runBlocking {
            database.dbQuery {
                setInnboksAktivFlag("1", false)
                var innboks = getInnboksByEventId("1")
                innboks.aktiv `should be equal to` false

                setInnboksAktivFlag("1", true)
                innboks = getInnboksByEventId("1")
                innboks.aktiv `should be equal to` true
            }
        }
    }

    @Test
    fun `should find Innboks by aktiv flag`() {
        runBlocking {
            database.dbQuery {
                setInnboksAktivFlag(innboks1.eventId, false)
                val aktiveInnboks = getAllInnboksByAktiv(true)
                val inaktivInnboks = getAllInnboksByAktiv(false)

                aktiveInnboks.none { it.id == innboks1.id }
                aktiveInnboks.size `should be equal to` allInnboks.size - 1
                inaktivInnboks.single { it.id == innboks1.id }
                inaktivInnboks.size `should be equal to` 1

                setInnboksAktivFlag(innboks1.eventId, true)
            }
        }
    }

    @Test
    fun `should find Innboks by aktorId`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksByAktorId(aktorId1)
                result.size `should be equal to` allInnboksForAktor1.size
                result `should contain all` allInnboksForAktor1
                result `should not contain` innboks2
            }
        }
    }

    @Test
    fun `should find Innboks by eventId`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksByEventId(innboks1.eventId)
                result `should equal` innboks1
            }
        }
    }
}