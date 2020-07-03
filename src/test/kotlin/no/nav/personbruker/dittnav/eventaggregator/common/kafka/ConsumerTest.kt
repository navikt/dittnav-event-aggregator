package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import org.amshove.kluent.`should equal`
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.DisconnectException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class ConsumerTest {

    private val kafkaConsumer = mockk<KafkaConsumer<Nokkel, Beskjed>>(relaxUnitFun = true)
    private val eventBatchProcessorService = mockk<EventBatchProcessorService<Beskjed>>(relaxed = true)

    @BeforeEach
    fun initMocks() {
        clearMocks()
        staticMocks()
    }
    fun clearMocks() {
        clearMocks(kafkaConsumer, eventBatchProcessorService)
    }

    fun staticMocks() {
        mockkStatic("no.nav.personbruker.dittnav.eventaggregator.common.kafka.KafkaConsumerKt")
    }

    @Test
    fun `Skal commit-e mot Kafka hvis ingen feil skjer`() {
        val topic = "dummyTopicNoErrors"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(1, topic)

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(300)

            consumer.status().status `should equal` Status.OK
            consumer.stopPolling()
        }
        verify(atLeast = 1) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal ikke kvittere ut eventer som lest, hvis en ukjent feil skjer`() {
        val topic = "dummyTopicUkjentFeil"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(1, topic)
        coEvery { eventBatchProcessorService.processEvents(any()) } throws Exception("Simulert feil i en test")

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(10)
            consumer.job.join()
            consumer.status().status `should equal` Status.ERROR
        }
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal ikke kvittere ut eventer som lest, hvis skriving mot cache-en feiler`() {
        val topic = "dummyTopicUnretriableErrorAgainstDb"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(1, topic)
        coEvery { eventBatchProcessorService.processEvents(any()) } throws UnretriableDatabaseException("Simulert feil i en test")

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(10)
            consumer.job.join()
            consumer.status().status `should equal` Status.ERROR
        }
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal ikke kvittere ut eventer som lest, hvis transformering av et eller flere eventer feiler`() {
        val topic = "dummyTopicUntransformable"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(1, topic)
        coEvery { eventBatchProcessorService.processEvents(any()) } throws UntransformableRecordException("Simulert feil i en test")

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(10)
            consumer.job.join()
            consumer.status().status `should equal` Status.ERROR
        }
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal fortsette pollingen hvis det er en retriable exception throw by Kafka`() {
        val topic = "dummyTopicKafkaRetriable"
        val retriableKafkaException = DisconnectException("Simulert feil i en test")
        every { kafkaConsumer.poll(any<Duration>()) } throws retriableKafkaException
        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)
        every { kafkaConsumer.rollbackToLastCommitted() } returns Unit

        runBlocking {
            consumer.startPolling()
            `Vent litt for aa bevise at det IKKE fortsettes aa polle`()

            consumer.status().status `should equal` Status.OK
            consumer.stopPolling()
        }
        verify(exactly = 0) { kafkaConsumer.commitSync() }
        verify(atLeast = 1) { kafkaConsumer.rollbackToLastCommitted() }
    }

    @Test
    fun `Skal fortsette pollingen hvis det er en retriable database exception`() {
        val topic = "dummyTopicDatabaseRetriable"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(1, topic)
        val retriableDbExption = RetriableDatabaseException("Simulert feil i en test")
        coEvery { eventBatchProcessorService.processEvents(any()) } throws retriableDbExption
        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)
        every { kafkaConsumer.rollbackToLastCommitted() } returns Unit


        runBlocking {
            consumer.startPolling()
            `Vent litt for aa bevise at det IKKE fortsettes aa polle`()

            consumer.status().status `should equal` Status.OK
            consumer.stopPolling()
        }
        verify(exactly = 0) { kafkaConsumer.commitSync() }
        verify(atLeast = 1) { kafkaConsumer.rollbackToLastCommitted() }
    }

    @Test
    fun `Skal ikke commit-e mot kafka hvis det IKKE har blitt funnet noen event-er`() {
        val topic = "dummyTopicNoRecordsFound"
        every { kafkaConsumer.poll(any<Duration>()) } returns ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(0, topic)

        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(30)

            consumer.status().status `should equal` Status.OK
            consumer.stopPolling()
        }
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    @Test
    fun `Skal ikke commit-e mot kafka hvis det har skjedd en CancellationException, som skjer ved stopping av polling`() {
        val topic = "dummyTopicCancellationException"
        val cancellationException = CancellationException("Simulert feil i en test")
        every { kafkaConsumer.poll(any<Duration>()) } throws cancellationException
        val consumer: Consumer<Beskjed> = Consumer(topic, kafkaConsumer, eventBatchProcessorService)

        runBlocking {
            consumer.startPolling()
            delay(10)
        }
        verify(exactly = 0) { kafkaConsumer.commitSync() }
    }

    private suspend fun `Vent litt for aa bevise at det IKKE fortsettes aa polle`() {
        delay(100)
    }

}
