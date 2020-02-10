package no.nav.personbruker.dittnav.eventaggregator.common.exceptions.kafka

import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import org.amshove.kluent.`should be equal to`
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.DisconnectException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class ConsumerTest {

    private val kafkaConsumer = mockk<KafkaConsumer<Nokkel, Beskjed>>(relaxUnitFun = true)
    private val eventBatchProcessorService = mockk<EventBatchProcessorService<Beskjed>>(relaxed = true)

    @BeforeEach
    fun clearMocks() {
        clearMocks(kafkaConsumer, eventBatchProcessorService)
    }

    @Test
    fun `Skal commit-e mot Kafka hvis ingen feil skjer`() {
        val topic = "dummyTopicNoErrors"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(1, topic)

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(30)
        }
        consumer.isRunning() `should be equal to` true
        consumer.stopPolling()
        verify(atLeast = 1) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal ikke kvittere ut eventer som lest, hvis en ukjent feil skjer`() {
        val topic = "dummyTopicUkjentFeil"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(1, topic)
        coEvery { eventBatchProcessorService.processEvents(any()) } throws Exception("Simulert feil i en test")

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(10)
            consumer.job.join()
        }
        consumer.isRunning() `should be equal to` false
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal ikke kvittere ut eventer som lest, hvis skriving mot cache-en feiler`() {
        val topic = "dummyTopicUnretriableErrorAgainstDb"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(1, topic)
        coEvery { eventBatchProcessorService.processEvents(any()) } throws UnretriableDatabaseException("Simulert feil i en test")

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(10)
            consumer.job.join()
        }
        consumer.isRunning() `should be equal to` false
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal ikke kvittere ut eventer som lest, hvis transformering av et eller flere eventer feiler`() {
        val topic = "dummyTopicUntransformable"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(1, topic)
        coEvery { eventBatchProcessorService.processEvents(any()) } throws UntransformableRecordException("Simulert feil i en test")

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(10)
            consumer.job.join()
        }
        consumer.isRunning() `should be equal to` false
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal fortsette pollingen hvis det er en retriable exception throw by Kafka`() {
        val topic = "dummyTopicKafkaRetriable"
        val retriableKafkaException = DisconnectException("Simulert feil i en test")
        every { kafkaConsumer.poll(any<Duration>()) } throws retriableKafkaException
        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            `Vent litt for aa bevise at det fortsettes aa polle`()
        }
        consumer.isRunning() `should be equal to` true
        consumer.stopPolling()
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal fortsette pollingen hvis det er en retriable database exception`() {
        val topic = "dummyTopicDatabaseRetriable"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(1, topic)
        val retriableDbExption = RetriableDatabaseException("Simulert feil i en test")
        coEvery { eventBatchProcessorService.processEvents(any()) } throws retriableDbExption
        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            `Vent litt for aa bevise at det fortsettes aa polle`()
        }
        consumer.isRunning() `should be equal to` true
        consumer.stopPolling()
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal alltid commit-e mot kafka hvis event-er har blitt funnet`() {
        val topic = "dummyTopicNoRecordsFound"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(0, topic)

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(30)
        }
        consumer.isRunning() `should be equal to` true
        consumer.stopPolling()
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    private suspend fun `Vent litt for aa bevise at det fortsettes aa polle`() {
        delay(10)
    }

}
