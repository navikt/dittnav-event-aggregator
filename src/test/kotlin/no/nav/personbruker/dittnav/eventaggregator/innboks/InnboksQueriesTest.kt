package no.nav.personbruker.dittnav.eventaggregator.innboks

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteBeskjedWithEventId
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getBeskjedByEventId
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.done.DoneObjectMother
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class InnboksQueriesTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val fodselsnummer1 = "12345"
    private val fodselsnummer2 = "67890"

    private val innboks1: Innboks
    private val innboks2: Innboks
    private val innboks3: Innboks
    private val innboksWithOffsetForstBehandlet: Innboks

    private val systembruker = "dummySystembruker"
    private val eventId = "1"

    private val allInnboks: List<Innboks>
    private val allInnboksForAktor1: List<Innboks>

    init {
        innboks1 = createInnboks("1", fodselsnummer1)
        innboks2 = createInnboks("2", fodselsnummer2)
        innboks3 = createInnboks("3", fodselsnummer1)
        innboksWithOffsetForstBehandlet = createInnboksWithOffsetForstBehandlet("4", fodselsnummer1)

        allInnboks = listOf(innboks1, innboks2, innboks3, innboksWithOffsetForstBehandlet)
        allInnboksForAktor1 = listOf(innboks1, innboks3, innboksWithOffsetForstBehandlet)
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


    private fun createInnboksWithOffsetForstBehandlet(eventId: String, fodselsnummer: String): Innboks {
        val offsetDate = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)
        val innboks = InnboksObjectMother.giveMeInnboksWithForstBehandlet(eventId, fodselsnummer, offsetDate)
        return runBlocking {
            database.dbQuery {
                createInnboks(innboks).entityId.let {
                    innboks.copy(id = it)
                }
            }
        }
    }

    @Test
    fun `finner alle Innboks`() {
        runBlocking {
            database.dbQuery {
                val result = getAllInnboks()
                result.size shouldBe allInnboks.size
                result shouldContainAll allInnboks
            }
        }
    }

    @Test
    fun `finner Innboks med id`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksById(innboks1.id!!)
                result shouldBe innboks1
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
                innboks.aktiv shouldBe false

                setInnboksEventerAktivFlag(listOf(doneEvent), true)
                innboks = getInnboksByEventId(eventId)
                innboks.aktiv shouldBe true
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
                aktiveInnboks.size shouldBe allInnboks.size - 1
                inaktivInnboks.single { it.id == innboks1.id }
                inaktivInnboks.size shouldBe 1

                setInnboksEventerAktivFlag(listOf(doneEvent), true)
            }
        }
    }

    @Test
    fun `finner Innboks med fodselsnummer`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksByFodselsnummer(fodselsnummer1)
                result.size shouldBe allInnboksForAktor1.size
                result shouldContainAll allInnboksForAktor1
                result shouldNotContain innboks2
            }
        }
    }

    @Test
    fun `finner Innboks med eventId`() {
        runBlocking {
            database.dbQuery {
                val result = getInnboksByEventId(innboks1.eventId)
                result shouldBe innboks1
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
                getAllInnboks().size shouldBe numberOfEvents
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

            innboks1FraDb.eventId shouldBe innboks1.eventId
            innboks2FraDb.eventId shouldBe innboks2.eventId

            database.dbQuery { deleteInnboksWithEventId(innboks1.eventId) }
            database.dbQuery { deleteInnboksWithEventId(innboks2.eventId) }
        }
    }

    @Test
    fun `Skal haandtere at prefererteKanaler er tom`() {
        val innboks = InnboksObjectMother.giveMeAktivInnboksWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        runBlocking {
            database.dbQuery { createInnboks(innboks) }
            val result = database.dbQuery { getInnboksByEventId(innboks.eventId) }
            result.prefererteKanaler.shouldBeEmpty()
            database.dbQuery { deleteInnboksWithEventId(innboks.eventId) }
        }

    }
}
