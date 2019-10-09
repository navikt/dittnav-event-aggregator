package no.nav.personbruker.dittnav.eventaggregator.kafka

import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import org.amshove.kluent.`should be equal to`
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.DisconnectException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class ConsumerTest {

    private val kafkaConsumer = mockk<KafkaConsumer<String, Informasjon>>(relaxUnitFun = true)
    private val eventBatchProcessorService = mockk<EventBatchProcessorService<Informasjon>>(relaxed = true)

    @BeforeEach
    fun clearMocks() {
        clearMocks(kafkaConsumer)
    }

    @Test
    fun `Skal commit-e mot Kafka hvis ingen feil skjer`() {
        val topic = "dummyTopicNoErrors"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(1, topic)

        val consumer: Consumer<Informasjon> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

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

        val consumer: Consumer<Informasjon> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

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

        val consumer: Consumer<Informasjon> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

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

        val consumer: Consumer<Informasjon> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

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
        val consumer: Consumer<Informasjon> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

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
        val consumer: Consumer<Informasjon> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            `Vent litt for aa bevise at det fortsettes aa polle`()
        }
        consumer.isRunning() `should be equal to` true
        consumer.stopPolling()
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    private suspend fun `Vent litt for aa bevise at det fortsettes aa polle`() {
        delay(10)
    }

}
