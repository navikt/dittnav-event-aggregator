package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameResolver
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
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
    private val metricsReporter = StubMetricsReporter()
    private val producerNameResolver = ProducerNameResolver(database)
    private val producerNameScrubber = ProducerNameScrubber(producerNameResolver)
    private val metricsProbe = EventMetricsProbe(metricsReporter, producerNameScrubber)
    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)
    private val doneEventService = DoneEventService(donePersistingService, metricsProbe)
    private val beskjed1 = BeskjedObjectMother.giveMeAktivBeskjed("1", "12345")
    private val oppgave = OppgaveObjectMother.giveMeAktivOppgave("2", "12345")
    private val innboks = InnboksObjectMother.giveMeAktivInnboks("3", "12345")
    private val beskjed2 = BeskjedObjectMother.giveMeAktivBeskjed("4", "12345")
    private val nokkel99 = createNokkel(99)

    init {
        runBlocking {
            database.dbQuery {
                createBeskjed(beskjed1)
                createBeskjed(beskjed2)
                createOppgave(oppgave)
                createInnboks(innboks)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllOppgave()
                deleteAllBeskjed()
                deleteAllInnboks()
                deleteAllDone()
            }
        }
    }

    @Test
    fun `Setter Beskjed-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord(Kafka.beskjedTopicName, 1, 1, createNokkel(eventId = 1), AvroDoneObjectMother.createDone(eventId = "1"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allBeskjed = database.dbQuery { getAllBeskjed() }
            val foundBeskjed = allBeskjed.first { it.eventId == "1" }
            foundBeskjed.aktiv.`should be false`()
        }
    }

    @Test
    fun `Setter Oppgave-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord(Kafka.oppgaveTopicName, 1, 1, createNokkel(eventId = 2), AvroDoneObjectMother.createDone(eventId = "2"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allOppgave = database.dbQuery { getAllOppgave() }
            val foundOppgave = allOppgave.first { it.eventId == "2" }
            foundOppgave.aktiv.`should be false`()
        }
    }

    @Test
    fun `Setter Innboks-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord<Nokkel, Done>(Kafka.innboksTopicName, 1, 1, createNokkel(eventId = 3), AvroDoneObjectMother.createDone(eventId = "3"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allInnboks = database.dbQuery { getAllInnboks() }
            val foundInnboks = allInnboks.first { it.eventId == "3" }
            foundInnboks.aktiv.`should be false`()
        }
    }

    @Test
    fun `Skal ikke lagre Done-event i cache hvis event med matchende eventId finnes`() {
        val record = ConsumerRecord(Kafka.beskjedTopicName, 1, 1, createNokkel(eventId = 4), AvroDoneObjectMother.createDone(eventId = "4"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.shouldBeEmpty()
        }
    }

    @Test
    fun `Lagrer Done-event i cache hvis event med matchende eventId ikke finnes`() {
        val record = ConsumerRecord(Kafka.beskjedTopicName, 1, 1, createNokkel(eventId = 5), AvroDoneObjectMother.createDone(eventId = "5"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.size `should be equal to` 1
            allDone.first().eventId `should be equal to` "5"
        }
    }

    @Test
    fun `Skal ikke lagre Done-event i cache på nytt hvis Done-event med samme id allerede er mottatt`() {
        val record1 = ConsumerRecord(Kafka.beskjedTopicName, 1, 1, createNokkel(eventId = 5), AvroDoneObjectMother.createDone(eventId = "5"))
        val record2 = ConsumerRecord(Kafka.beskjedTopicName, 1, 1, createNokkel(eventId = 5), AvroDoneObjectMother.createDone(eventId = "5"))
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(listOf(record1, record2))
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.size `should be equal to` 1
            allDone.first().eventId `should be equal to` "5"
        }
    }
}
