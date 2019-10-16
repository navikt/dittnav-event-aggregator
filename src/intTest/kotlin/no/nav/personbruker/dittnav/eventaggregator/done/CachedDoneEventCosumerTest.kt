package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.informasjon.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.informasjon.createInformasjon
import no.nav.personbruker.dittnav.eventaggregator.informasjon.deleteAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.informasjon.getInformasjonByEventId
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getOppgaveByEventId
import org.amshove.kluent.AnyException
import org.amshove.kluent.`should not throw`
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeFalse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class CachedDoneEventCosumerTest {

    private val database = H2Database()
    private val eventConsumer = CachedDoneEventConsumer(database = database)

    private val informasjon1 = InformasjonObjectMother.createInformasjon("1", "12345")
    private val oppgave1 = OppgaveObjectMother.createOppgave("2", "12345")
    private val done1 = DoneObjectMother.createDone("3")
    private val done2 = DoneObjectMother.createDone("4")

    init {
        runBlocking {
            database.dbQuery {
                createInformasjon(informasjon1)
                createOppgave(oppgave1)
                createDoneEvent(done1)
                createDoneEvent(done2)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        eventConsumer.cancel()
        runBlocking {
            database.dbQuery {
                deleteAllInformasjon()
                deleteAllOppgave()
                deleteAllDone()
            }
        }
    }

    @Test
    fun `setter Informasjon-event inaktivt hvis Done-event med samme eventId tidligere er mottatt`() {
        runBlocking {
            database.dbQuery { createInformasjon(InformasjonObjectMother.createInformasjon("3", "12345")) }
            eventConsumer.processDoneEvents()
            val informasjon = database.dbQuery { getInformasjonByEventId("3") }
            informasjon.aktiv.shouldBeFalse()
        }
    }

    @Test
    fun `setter Oppgave-event inaktivt hvis Done-event med samme eventId tidligere er mottatt`() {
        runBlocking {
            database.dbQuery { createOppgave(OppgaveObjectMother.createOppgave("4", "12345")) }
            eventConsumer.processDoneEvents()
            val oppgave = database.dbQuery { getOppgaveByEventId("4") }
            oppgave.aktiv.shouldBeFalse()
        }
    }

    @Test
    fun `feiler ikke hvis event med samme eventId som Done-event ikke er mottatt`() {
        invoking {
            runBlocking {
                database.dbQuery { createDoneEvent(DoneObjectMother.createDone("-1")) }
                eventConsumer.processDoneEvents()
            }
        } `should not throw` AnyException
    }
}
