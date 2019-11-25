package no.nav.personbruker.dittnav.eventaggregator.innboks

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgave
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test


class InnboksQueriesTest {
    private val database = H2Database()

    private val fodselsnummer1 = "12345"
    private val fodselsnummer2 = "67890"

    private val innboks1: Innboks
    private val innboks2: Innboks
    private val innboks3: Innboks

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
        val innboks = InnboksObjectMother.createInnboks(eventId, fodselsnummer)

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
                result `should equal` innboks1
            }
        }
    }

    @Test
    fun `setter aktiv flag`() {
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
    fun `finner Innboks etter aktiv flag`() {
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
                result `should equal` innboks1
            }
        }
    }

    @Test
    fun `persister ikke entitet dersom rad med samme eventId og produsent finnes`() {
        runBlocking {
            database.dbQuery {
                createInnboks(innboks1)
                getAllInnboks().size `should be equal to` allInnboks.size
            }
        }
    }
}