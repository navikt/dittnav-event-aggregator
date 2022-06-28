package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.KafkaTestTopics
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.getAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.nokkel.AvroNokkelInternObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgave
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoneEventServiceTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)
    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)
    private val doneEventService = DoneEventService(donePersistingService, metricsProbe)
    private val beskjed1 = BeskjedObjectMother.giveMeAktivBeskjed("1", "12345")
    private val oppgave = OppgaveObjectMother.giveMeAktivOppgave("2", "12345")
    private val innboks = InnboksObjectMother.giveMeAktivInnboks("3", "12345")
    private val beskjed2 = BeskjedObjectMother.giveMeAktivBeskjed("4", "12345")

    @BeforeEach
    fun resetMocks() {
        runBlocking {
            database.dbQuery {
                createBeskjed(beskjed1)
                createBeskjed(beskjed2)
                createOppgave(oppgave)
                createInnboks(innboks)
            }
        }
    }

    @AfterEach
    fun cleanUp() {
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
        val record = ConsumerRecord(KafkaTestTopics.beskjedInternTopicName, 1, 1, AvroNokkelInternObjectMother.createNokkelWithEventId(eventId = 1), AvroDoneObjectMother.createDone())
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allBeskjed = database.dbQuery { getAllBeskjed() }
            allBeskjed.first { it.eventId == "1" }.aktiv shouldBe false
            allBeskjed.count { !it.aktiv } shouldBe 1
        }
    }

    @Test
    fun `Setter Oppgave-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord(KafkaTestTopics.oppgaveInternTopicName, 1, 1, AvroNokkelInternObjectMother.createNokkelWithEventId(eventId = 2), AvroDoneObjectMother.createDone())
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allOppgave = database.dbQuery { getAllOppgave() }
            allOppgave.first { it.eventId == "2" }.aktiv shouldBe false
            allOppgave.count { !it.aktiv } shouldBe 1

        }
    }

    @Test
    fun `Setter Innboks-event inaktivt hvis Done-event mottas`() {
        val record = ConsumerRecord(KafkaTestTopics.innboksInternTopicName, 1, 1, AvroNokkelInternObjectMother.createNokkelWithEventId(eventId = 3), AvroDoneObjectMother.createDone())
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allInnboks = database.dbQuery { getAllInnboks() }
            allInnboks.first { it.eventId == "3" }.aktiv shouldBe false
            allInnboks.count { !it.aktiv } shouldBe 1
        }
    }

    @Test
    fun `Skal ikke lagre Done-event i cache hvis event med matchende eventId finnes`() {
        val record = ConsumerRecord(KafkaTestTopics.beskjedInternTopicName, 1, 1, AvroNokkelInternObjectMother.createNokkelWithEventId(eventId = 4), AvroDoneObjectMother.createDone())
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.shouldBeEmpty()
        }
    }

    @Test
    fun `Lagrer Done-event i cache hvis event med matchende eventId ikke finnes`() {
        val record = ConsumerRecord(KafkaTestTopics.beskjedInternTopicName, 1, 1, AvroNokkelInternObjectMother.createNokkelWithEventId(eventId = 5), AvroDoneObjectMother.createDone())
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(record)
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.size shouldBe 1
            allDone.first().eventId shouldBe "5"
        }
    }

    @Test
    fun `Skal ikke lagre Done-event i cache paa nytt hvis Done-event med samme id allerede er mottatt`() {
        val record1 = ConsumerRecord(KafkaTestTopics.beskjedInternTopicName, 1, 1, AvroNokkelInternObjectMother.createNokkelWithEventId(eventId = 5), AvroDoneObjectMother.createDone())
        val record2 = ConsumerRecord(KafkaTestTopics.beskjedInternTopicName, 1, 1, AvroNokkelInternObjectMother.createNokkelWithEventId(eventId = 5), AvroDoneObjectMother.createDone())
        val records = ConsumerRecordsObjectMother.wrapInConsumerRecords(listOf(record1, record2), "doneTopic")
        runBlocking {
            doneEventService.processEvents(records)
            val allDone = database.dbQuery { getAllDoneEvent() }
            allDone.size shouldBe 1
            allDone.first().eventId shouldBe "5"
        }
    }
}
