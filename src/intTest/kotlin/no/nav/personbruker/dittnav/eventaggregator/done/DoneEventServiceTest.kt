package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.informasjon.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.informasjon.createInformasjon
import no.nav.personbruker.dittnav.eventaggregator.informasjon.deleteAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.informasjon.getAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.melding.MeldingObjectMother
import no.nav.personbruker.dittnav.eventaggregator.melding.createMelding
import no.nav.personbruker.dittnav.eventaggregator.melding.deleteAllMelding
import no.nav.personbruker.dittnav.eventaggregator.melding.getAllMelding
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgave
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.shouldBeEmpty
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class DoneEventServiceTest {

    private val database = H2Database()
    private val doneEventService = DoneEventService(database)
    private val informasjon1 = InformasjonObjectMother.createInformasjon("1", "12345")
    private val oppgave = OppgaveObjectMother.createOppgave("2", "12345")
    private val melding = MeldingObjectMother.createMelding("3", "12345")
    private val informasjon2 = InformasjonObjectMother.createInformasjon("4", "12345")

    init {
        runBlocking {
            database.dbQuery {
                createInformasjon(informasjon1)
                createInformasjon(informasjon2)
                createOppgave(oppgave)
                createMelding(melding)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllOppgave()
                deleteAllInformasjon()
                deleteAllMelding()
                deleteAllDone()
            }
        }
    }

    @Test
    fun `Setter Informasjon-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord<String, Done>(Kafka.informasjonTopicName, 1, 1, null, AvroDoneObjectMother.createDone("1"))
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
        val record = ConsumerRecord<String, Done>(Kafka.oppgaveTopicName, 1, 1, null, AvroDoneObjectMother.createDone("2"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allOppgave = database.dbQuery { getAllOppgave() }
            val foundOppgave = allOppgave.first { it.eventId == "2" }
            foundOppgave.aktiv.`should be false`()
        }
    }

    @Test
    fun `Flag Melding event as inactive if Done event is received`() {
        val record = ConsumerRecord<String, Done>(Kafka.meldingTopicName, 1, 1, null, AvroDoneObjectMother.createDone("3"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allOppgave = database.dbQuery { getAllMelding() }
            val foundOppgave = allOppgave.first { it.eventId == "3" }
            foundOppgave.aktiv.`should be false`()
        }
    }

    @Test
    fun `Should not cache Done event if event with matching eventId exists`() {
        val record = ConsumerRecord<String, Done>(Kafka.informasjonTopicName, 1, 1, null, AvroDoneObjectMother.createDone("4"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.shouldBeEmpty()
        }
    }

    @Test
    fun `Lagrer Done-event i cache hvis event med matchende eventId ikke finnes`() {
        val record = ConsumerRecord<String, Done>(Kafka.informasjonTopicName, 1, 1, null, AvroDoneObjectMother.createDone("99"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.size `should be equal to` 1
            allDone.first().eventId `should be equal to` "99"
        }
    }
}
