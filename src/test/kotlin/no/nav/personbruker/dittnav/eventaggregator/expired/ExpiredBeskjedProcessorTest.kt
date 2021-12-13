package no.nav.personbruker.dittnav.eventaggregator.expired

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.KafkaProducerWrapper
import org.amshove.kluent.shouldBe
import org.apache.kafka.clients.producer.MockProducer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ExpiredBeskjedProcessorTest {

    private val producer = MockProducer<Nokkel, Done>()
    private val expiredPersistingService = mockk<ExpiredPersistingService>(relaxed = true)
    private val doneEmitter = DoneEventEmitter(KafkaProducerWrapper("test", producer))
    private val processor = ExpiredBeskjedProcessor(expiredPersistingService, doneEmitter)

    @BeforeEach
    fun `reset mocks`() {
        clearMocks(expiredPersistingService)
        producer.clear()
    }

    @Test
    fun `skal sende done-eventer for hver utgaatt beskjed`() {
        val result = listOf(
            BeskjedObjectMother.giveMeAktivBeskjed().copy(id = 1),
            BeskjedObjectMother.giveMeAktivBeskjed().copy(id = 2)
        )
        coEvery { expiredPersistingService.getExpiredBeskjeder()
        } returns result andThen listOf()

        runBlocking {
            processor.sendDoneEventsForExpiredBeskjeder()
        }

        producer.history().size shouldBe 2
    }

    @Test
    fun `Hvis ingen beskjed har utgaatt, ingen done-event skal bli sent`() {
        coEvery { expiredPersistingService.getExpiredBeskjeder() } returns listOf()

        producer.history().size shouldBe 0
    }
}
