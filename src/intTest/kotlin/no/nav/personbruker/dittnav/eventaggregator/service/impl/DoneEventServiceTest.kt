package no.nav.personbruker.dittnav.eventaggregator.service.impl

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.*
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.DoneObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class DoneEventServiceTest {

    private val database = H2Database()
    private val doneEventService = DoneEventService(database)
    private val informasjon = InformasjonObjectMother.createInformasjon("1", "12345")
    private val oppgave = OppgaveObjectMother.createOppgave("2", "12345")

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
        val record = ConsumerRecord<String, Done>(Kafka.informasjonTopicName, 1, 1, null, DoneObjectMother.createDone("1"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allInformasjon = database.dbQuery { getAllInformasjon() }
            val foundInformasjon = allInformasjon.first { it.eventId == "1" }
            foundInformasjon.aktiv.`should be false`()
        }
    }

    @Test
    fun `Setter Oppgave-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord<String, Done>(Kafka.oppgaveTopicName, 1, 1, null, DoneObjectMother.createDone("2"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allOppgave = database.dbQuery { getAllOppgave() }
            val foundOppgave = allOppgave.first { it.eventId == "2" }
            foundOppgave.aktiv.`should be false`()
        }
    }

    @Test
    fun `Lagrer Done-event i cache hvis event med matchende eventId ikke finnes`() {
        val record = ConsumerRecord<String, Done>(Kafka.informasjonTopicName, 1, 1, null, DoneObjectMother.createDone("3"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.size `should be equal to` 1
            allDone.first().eventId `should be equal to` "3"
        }
    }
}
