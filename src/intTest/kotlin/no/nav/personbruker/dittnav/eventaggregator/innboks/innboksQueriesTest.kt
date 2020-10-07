package no.nav.personbruker.dittnav.eventaggregator.innboks

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.done.DoneObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not contain`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class innboksQueriesTest {
    private val database = H2Database()

    private val fodselsnummer1 = "12345"
    private val fodselsnummer2 = "67890"

    private val innboks1: Innboks
    private val innboks2: Innboks
    private val innboks3: Innboks

    private val systembruker = "dummySystembruker"
    private val eventId = "1"

    private val allInnboks: List<Innboks>
    private val allInnboksForAktor1: List<Innboks>

    init {
        innboks1 = createInnboks("1", fodselsnummer1)
        innboks2 = createInnboks("2", fodselsnummer2)
        innboks3 = createInnboks("3", fodselsnummer1)

        allInnboks = listOf(innboks1, innboks2, innboks3)
        allInnboksForAktor1 = listOf(innboks1, innboks3)
    }

    private fun createInnboks(eventId: String, fodselsnummer: String): Innboks {
        val innboks = InnboksObjectMother.giveMeAktivInnboks(eventId, fodselsnummer)

        return runBlocking {
            database.dbQuery {
                createInnboks(innboks).entityId.let {
                    innboks.copy(id = it)
                }
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
    fun `finner alle Innboks`() {
        runBlocking {
            database.dbQuery {
                val result = getAllInnboks()
                result.size `should be equal to` allInnboks.size
                result `should contain all` allInnboks
            }
        }
    }

    @Test
    fun `finner Innboks med id`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksById(innboks1.id!!)
                result `should be equal to` innboks1
            }
        }
    }

    @Test
    fun `setter aktiv flag`() {
        val doneEvent = DoneObjectMother.giveMeDone(eventId, systembruker, fodselsnummer1)
        runBlocking {
            database.dbQuery {
                setInnboksEventerAktivFlag(listOf(doneEvent), false)
                var innboks = getInnboksByEventId(eventId)
                innboks.aktiv `should be equal to` false

                setInnboksEventerAktivFlag(listOf(doneEvent), true)
                innboks = getInnboksByEventId(eventId)
                innboks.aktiv `should be equal to` true
            }
        }
    }

    @Test
    fun `finner Innboks etter aktiv flag`() {
        val doneEvent = DoneObjectMother.giveMeDone(innboks1.eventId, systembruker, fodselsnummer1)
        runBlocking {
            database.dbQuery {
                setInnboksEventerAktivFlag(listOf(doneEvent), false)
                val aktiveInnboks = getAllInnboksByAktiv(true)
                val inaktivInnboks = getAllInnboksByAktiv(false)

                aktiveInnboks.none { it.id == innboks1.id }
                aktiveInnboks.size `should be equal to` allInnboks.size - 1
                inaktivInnboks.single { it.id == innboks1.id }
                inaktivInnboks.size `should be equal to` 1

                setInnboksEventerAktivFlag(listOf(doneEvent), true)
            }
        }
    }

    @Test
    fun `finner Innboks med fodselsnummer`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksByFodselsnummer(fodselsnummer1)
                result.size `should be equal to` allInnboksForAktor1.size
                result `should contain all` allInnboksForAktor1
                result `should not contain` innboks2
            }
        }
    }

    @Test
    fun `finner Innboks med eventId`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksByEventId(innboks1.eventId)
                result `should be equal to` innboks1
            }
        }
    }

    @Test
    fun `persister ikke entitet dersom rad med samme eventId og produsent finnes`() {
        runBlocking {
            database.dbQuery {
                createInnboks(innboks1)
                val numberOfEvents = getAllInnboks().size
                createInnboks(innboks1)
                getAllInnboks().size `should be equal to` numberOfEvents
            }
        }
    }

    @Test
    fun `Skal skrive eventer i batch`() {
        val innboks1 = InnboksObjectMother.giveMeAktivInnboks("i-1", "123")
        val innboks2 = InnboksObjectMother.giveMeAktivInnboks("i-2", "123")

        runBlocking {
            database.dbQuery {
                createInnboksEventer(listOf(innboks1, innboks2))
            }

            val innboks1FraDb = database.dbQuery { getInnboksByEventId(innboks1.eventId) }
            val innboks2FraDb = database.dbQuery { getInnboksByEventId(innboks2.eventId) }

            innboks1FraDb.eventId `should be equal to` innboks1.eventId
            innboks2FraDb.eventId `should be equal to` innboks2.eventId

            database.dbQuery { deleteInnboksWithEventId(innboks1.eventId) }
            database.dbQuery { deleteInnboksWithEventId(innboks2.eventId) }
        }
    }
}
