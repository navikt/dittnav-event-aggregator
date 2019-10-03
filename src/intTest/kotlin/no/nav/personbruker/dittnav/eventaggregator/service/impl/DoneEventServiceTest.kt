package no.nav.personbruker.dittnav.eventaggregator.service.impl

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.*
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.DoneObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeFalse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class DoneEventServiceTest {

    val database = H2Database()
    val doneEventService = DoneEventService(database)
    val informasjon = InformasjonObjectMother.createInformasjon(1, "12345")
    val oppgave = OppgaveObjectMother.createOppgave(2, "12345")

    init {
        runBlocking {
            database.dbQuery {
                createInformasjon(informasjon)
                createOppgave(oppgave)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllOppgave()
                deleteAllInformasjon()
            }
        }
    }

    @Test
    fun `Setter Informasjon-event inaktivt hvis Done-event mottas`() {
        val record =  ConsumerRecord<String, Done>(Kafka.informasjonTopicName, 1, 1, null, DoneObjectMother.createDone("1"));
        doneEventService.processEvent(record)
        runBlocking {
            val allInformasjon = database.dbQuery { getAllInformasjon() }
            val foundInformasjon = allInformasjon.first { it.eventId == "1" }
            foundInformasjon.aktiv.`shouldBeFalse`()
        }
    }

    @Test
    fun `Setter Oppgave-event inaktivt hvis Done-event mottas`() {
        val record =  ConsumerRecord<String, Done>(Kafka.oppgaveTopicName, 1, 1, null, DoneObjectMother.createDone("2"));
        doneEventService.processEvent(record)
        runBlocking {
            val allOppgave = database.dbQuery { getAllOppgave() }
            val foundOppgave = allOppgave.first { it.eventId == "2" }
            foundOppgave.aktiv.`shouldBeFalse`()
        }
    }

    @Test
    fun `Lagrer Done-event i cache hvis event med matchende eventId ikke finnes`() {
        val record =  ConsumerRecord<String, Done>(Kafka.informasjonTopicName, 1, 1, null, DoneObjectMother.createDone("3"));
        doneEventService.processEvent(record)
        runBlocking {
            val allDone = database.dbQuery { getAllDone() }
            allDone.size `should be equal to` 1
            allDone.first().eventId `should be equal to` "3"
        }
    }
}
