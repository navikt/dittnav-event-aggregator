package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.*
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

internal class metricsQueriesTest {

    private val database = H2Database()

    private val beskjed1: Beskjed
    private val beskjed2: Beskjed
    private val beskjed3: Beskjed
    private val beskjed4: Beskjed

    private val allEventsForSingleSystembruker: List<Beskjed>

    init {
        beskjed1 = createBeskjed(BeskjedObjectMother.giveMeAktivBeskjed("1", "12345"))
        beskjed2 = createBeskjed(BeskjedObjectMother.giveMeAktivBeskjed("2", "12345"))
        beskjed3 = createBeskjed(BeskjedObjectMother.giveMeAktivBeskjed("3", "12345"))
        beskjed4 = createBeskjed(BeskjedObjectMother.giveMeAktivBeskjed("4", "6789"))
        allEventsForSingleSystembruker = listOf(beskjed1, beskjed2, beskjed3, beskjed4)
    }

    private fun createBeskjed(beskjed: Beskjed): Beskjed {
        return runBlocking {
            database.dbQuery {
                createBeskjed(beskjed).entityId.let {
                    beskjed.copy(id = it)
                }
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
        }
    }

    @Test
    fun `Skal telle det totale antall beskjeder`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEvents(EventType.BESKJED)
            }
        } `should be equal to` allEventsForSingleSystembruker.size.toLong()
    }

    @Test
    fun `Skal telle det totale antall aktive beskjeder`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEventsByActiveStatus(EventType.BESKJED, true)
            }
        } `should be equal to` allEventsForSingleSystembruker.size.toLong()
    }

    @Test
    fun `Skal telle det totale antall inaktive beskjeder`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEventsByActiveStatus(EventType.BESKJED, false)
            }
        } `should be equal to` 0
    }

    @Test
    fun `Skal hente totalantallet eventer per produsent`() {
        val beskjedFraAnnenSystembruker = createBeskjed(BeskjedObjectMother.giveMeAktivBeskjed("5", "123", "enAnnenSystembruker"))

        val eventerPerSystembruker =
                runBlocking {
                    database.dbQuery {
                        countTotalNumberOfEventsGroupedBySystembruker(EventType.BESKJED)
                    }
                }

        eventerPerSystembruker.size `should be equal to` 2
        eventerPerSystembruker[beskjed1.systembruker]?.`should be equal to`(allEventsForSingleSystembruker.size)
        eventerPerSystembruker[beskjedFraAnnenSystembruker.systembruker]?.`should be equal to`(1)
    }

    @Test
    fun `Skal hente totalantallet brukernotifikasjoner per produsent basert paa status`() {
        val inaktivBeskjed = createBeskjed(BeskjedObjectMother.giveMeInaktivBeskjed())

        val aktiveBrukernotifikasjonerPerProdusent =
                runBlocking {
                    database.dbQuery {
                        countTotalNumberOfBrukernotifikasjonerByActiveStatus(true)
                    }
                }

        val inaktiveBrukernotifikasjonerPerProdusent =
                runBlocking {
                    database.dbQuery {
                        countTotalNumberOfBrukernotifikasjonerByActiveStatus(false)
                    }
                }

        aktiveBrukernotifikasjonerPerProdusent.size `should be equal to` 1
        aktiveBrukernotifikasjonerPerProdusent[beskjed1.systembruker]?.`should be equal to`(allEventsForSingleSystembruker.size)
        inaktiveBrukernotifikasjonerPerProdusent.size `should be equal to` 1
        inaktiveBrukernotifikasjonerPerProdusent[inaktivBeskjed.systembruker]?.`should be equal to`(1)

        `delete beskjed with eventId`(inaktivBeskjed.eventId)
    }

    private fun `delete beskjed with eventId`(eventId: String) {
        runBlocking {
            database.dbQuery {
                deleteBeskjedWithEventId(eventId)
            }
        }
    }

}
