package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.consumer

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.InnboksIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.createEventRecords
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.delayUntilDone
import no.nav.personbruker.dittnav.eventaggregator.innboks.AvroInnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.AvroOppgaveObjectMother
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

class MultipleTopicsConsumerTest {

    private val beskjedProcessor = SimpleEventCounterService<BeskjedIntern>()
    private val oppgaveProcessor = SimpleEventCounterService<OppgaveIntern>()
    private val innboksProcessor = SimpleEventCounterService<InnboksIntern>()

    private val beskjedTopicPartition = TopicPartition("beskjed", 0)
    private val oppgaveTopicPartition = TopicPartition("oppgave", 0)
    private val innboksTopicPartition = TopicPartition("innboks", 0)

    private val beskjedConsumerMock = MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(beskjedTopicPartition.topic()))
        it.rebalance(listOf(beskjedTopicPartition))
        it.updateBeginningOffsets(mapOf(beskjedTopicPartition to 0))
    }

    private val oppgaveConsumerMock = MockConsumer<NokkelIntern, OppgaveIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(oppgaveTopicPartition.topic()))
        it.rebalance(listOf(oppgaveTopicPartition))
        it.updateBeginningOffsets(mapOf(oppgaveTopicPartition to 0))
    }

    private val innboksConsumerMock = MockConsumer<NokkelIntern, InnboksIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(innboksTopicPartition.topic()))
        it.rebalance(listOf(innboksTopicPartition))
        it.updateBeginningOffsets(mapOf(innboksTopicPartition to 0))
    }

    private val beskjedConsumer = Consumer(beskjedTopicPartition.topic(), beskjedConsumerMock, beskjedProcessor)
    private val oppgaveConsumer = Consumer(oppgaveTopicPartition.topic(), oppgaveConsumerMock, oppgaveProcessor)
    private val innboksConsumer = Consumer(innboksTopicPartition.topic(), innboksConsumerMock, innboksProcessor)

    @Test
    fun `Skal kunne konsumere fra flere topics i parallell 2`() {
        val beskjeder = createEventRecords(4, beskjedTopicPartition, AvroBeskjedObjectMother::createBeskjed)
        val oppgaver = createEventRecords(5, oppgaveTopicPartition, AvroOppgaveObjectMother::createOppgave)
        val innbokser = createEventRecords(6, innboksTopicPartition, AvroInnboksObjectMother::createInnboks)

        runBlocking {
            beskjedConsumer.startPolling()
            oppgaveConsumer.startPolling()
            innboksConsumer.startPolling()

            beskjeder.forEach { beskjedConsumerMock.addRecord(it) }
            oppgaver.forEach { oppgaveConsumerMock.addRecord(it) }
            innbokser.forEach { innboksConsumerMock.addRecord(it) }

            delayUntilDone(beskjedConsumer, beskjeder.size)
            delayUntilDone(oppgaveConsumer, oppgaver.size)
            delayUntilDone(innboksConsumer, innbokser.size)

            beskjedConsumer.stopPolling()
            oppgaveConsumer.stopPolling()
            innboksConsumer.stopPolling()

            beskjeder.size shouldBe beskjedProcessor.eventCounter
            oppgaver.size shouldBe oppgaveProcessor.eventCounter
            innbokser.size shouldBe innboksProcessor.eventCounter
        }
    }
}
