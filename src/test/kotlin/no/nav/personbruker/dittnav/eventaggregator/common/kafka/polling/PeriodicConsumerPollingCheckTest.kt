package no.nav.personbruker.dittnav.eventaggregator.common.kafka.polling

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PeriodicConsumerPollingCheckTest {

    private val appContext = mockk<ApplicationContext>(relaxed = true)
    private val periodicConsumerPollingCheck = PeriodicConsumerPollingCheck(appContext)

    @BeforeEach
    fun resetMocks() {
        mockkObject(KafkaConsumerSetup)
        coEvery { KafkaConsumerSetup.restartPolling(appContext) } returns Unit
        coEvery { KafkaConsumerSetup.stopAllKafkaConsumers(appContext) } returns Unit
        coEvery { appContext.reinitializeConsumers() } returns Unit
        coEvery { KafkaConsumerSetup.startAllKafkaPollers(appContext) } returns Unit
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Skal returnere en liste med konsumenter som har stoppet aa polle`() {
        coEvery { appContext.beskjedConsumer.isStopped() } returns true
        coEvery { appContext.doneConsumer.isStopped() } returns true
        coEvery { appContext.oppgaveConsumer.isStopped() } returns false

        runBlocking {
            periodicConsumerPollingCheck.getConsumersThatHaveStopped().size shouldBe 2
        }
    }

    @Test
    fun `Skal returnere en tom liste hvis alle konsumenter kjorer som normalt`() {
        coEvery { appContext.beskjedConsumer.isStopped() } returns false
        coEvery { appContext.doneConsumer.isStopped() } returns false
        coEvery { appContext.oppgaveConsumer.isStopped() } returns false

        runBlocking {
            periodicConsumerPollingCheck.getConsumersThatHaveStopped().shouldBeEmpty()
        }
    }

    @Test
    fun `Skal kalle paa restartPolling hvis en eller flere konsumere har sluttet aa kjore`() {
        coEvery { appContext.beskjedConsumer.isStopped() } returns true
        coEvery { appContext.doneConsumer.isStopped() } returns false
        coEvery { appContext.oppgaveConsumer.isStopped() } returns true

        runBlocking {
            periodicConsumerPollingCheck.checkIfConsumersAreRunningAndRestartIfNot()
        }

        coVerify(exactly = 1) { KafkaConsumerSetup.restartPolling(appContext) }
        confirmVerified(KafkaConsumerSetup)
    }

    @Test
    fun `Skal ikke restarte polling hvis alle konsumere kjorer`() {
        coEvery { appContext.beskjedConsumer.isStopped() } returns false
        coEvery { appContext.doneConsumer.isStopped() } returns false
        coEvery { appContext.oppgaveConsumer.isStopped() } returns false

        runBlocking {
            periodicConsumerPollingCheck.checkIfConsumersAreRunningAndRestartIfNot()
        }

        coVerify(exactly = 0) { KafkaConsumerSetup.restartPolling(appContext) }
        confirmVerified(KafkaConsumerSetup)
    }
}