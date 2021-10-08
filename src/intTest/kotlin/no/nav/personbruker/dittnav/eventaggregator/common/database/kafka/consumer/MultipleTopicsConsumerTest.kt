package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.consumer

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.InnboksIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.config.KafkaEmbed
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.KafkaTestTopics
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaTestUtil
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheBeskjedTopic
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheInnboksTopic
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheOppgaveTopic
import no.nav.personbruker.dittnav.eventaggregator.innboks.AvroInnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import no.nav.personbruker.dittnav.eventaggregator.oppgave.AvroOppgaveObjectMother
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class MultipleTopicsConsumerTest {

    private val topics = listOf(KafkaTestTopics.beskjedInternTopicName, KafkaTestTopics.oppgaveInternTopicName, KafkaTestTopics.innboksInternTopicName)
    private val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(topics)
    private val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)
    private val adminClient = embeddedEnv.adminClient

    private val beskjedEventProcessor = SimpleEventCounterService<BeskjedIntern>()
    private val oppgaveEventProcessor = SimpleEventCounterService<OppgaveIntern>()
    private val innboksEventProcessor = SimpleEventCounterService<InnboksIntern>()

    private val beskjedEvents = (1..10).map { createNokkel(it) to AvroBeskjedObjectMother.createBeskjed(it) }.toMap()
    private val oppgaveEvents = (1..11).map { createNokkel(it) to AvroOppgaveObjectMother.createOppgave(it) }.toMap()
    private val innboksEvents = (1..12).map { createNokkel(it) to AvroInnboksObjectMother.createInnboks(it) }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun tearDown() {
        adminClient?.close()
        embeddedEnv.tearDown()
    }

    @Test
    fun `Kafka instansen i minnet har blitt startet`() {
        embeddedEnv.serverPark.status `should be equal to` KafkaEnvironment.ServerParkStatus.Started
    }

    fun `Produserer testeventer for alle topics`() {
        runBlocking {
            val producedAllBeskjed = KafkaTestUtil.produceEvents(testEnvironment, KafkaTestTopics.beskjedInternTopicName, beskjedEvents)
            val producedAllOppgave = KafkaTestUtil.produceEvents(testEnvironment, KafkaTestTopics.oppgaveInternTopicName, oppgaveEvents)
            val producedAllInnboks = KafkaTestUtil.produceEvents(testEnvironment, KafkaTestTopics.innboksInternTopicName, innboksEvents)
            producedAllBeskjed `should be equal to` true
            producedAllOppgave `should be equal to` true
            producedAllInnboks `should be equal to` true
        }
    }

    @Test
    fun `Skal kunne konsumere fra flere topics i parallell`() {
        `Produserer testeventer for alle topics`()

        val beskjedConsumer = createInfoConsumer(testEnvironment, beskjedEventProcessor)
        val oppgaveConsumer = createOppgaveConsumer(testEnvironment, oppgaveEventProcessor)
        val innboksConsumer = createInnboksConsumer(testEnvironment, innboksEventProcessor)

        runBlocking {
            beskjedConsumer.startPolling()
            oppgaveConsumer.startPolling()
            innboksConsumer.startPolling()

            `Vent til alle eventer har blitt konsumert`()

            beskjedConsumer.stopPolling()
            oppgaveConsumer.stopPolling()
            innboksConsumer.stopPolling()

            beskjedEvents.size `should be equal to` beskjedEventProcessor.eventCounter
            oppgaveEvents.size `should be equal to` oppgaveEventProcessor.eventCounter
            innboksEvents.size `should be equal to` innboksEventProcessor.eventCounter
        }
    }

    private suspend fun `Vent til alle eventer har blitt konsumert`() {
        while (`Har alle eventer blitt lest`(beskjedEventProcessor, beskjedEvents,
                        oppgaveEventProcessor, oppgaveEvents,
                        innboksEventProcessor, innboksEvents)) {
            delay(100)
        }
    }

    private fun `Har alle eventer blitt lest`(beskjed: SimpleEventCounterService<BeskjedIntern>, BeskjedEvents: Map<NokkelIntern, BeskjedIntern>,
                                              oppgave: SimpleEventCounterService<OppgaveIntern>, oppgaveEvents: Map<NokkelIntern, OppgaveIntern>,
                                              innboks: SimpleEventCounterService<InnboksIntern>, innboksEvents: Map<NokkelIntern, InnboksIntern>): Boolean {
        return beskjed.eventCounter < BeskjedEvents.size &&
                oppgave.eventCounter < oppgaveEvents.size &&
                innboks.eventCounter < innboksEvents.size
    }

    private fun createInfoConsumer(env: Environment, BeskjedEventProcessor: SimpleEventCounterService<BeskjedIntern>): Consumer<BeskjedIntern> {
        val kafkaProps = KafkaEmbed.consumerProps(env, EventType.BESKJED_INTERN)
        return setupConsumerForTheBeskjedTopic(kafkaProps, BeskjedEventProcessor, KafkaTestTopics.beskjedInternTopicName)
    }

    private fun createOppgaveConsumer(env: Environment, oppgaveEventProcessor: SimpleEventCounterService<OppgaveIntern>): Consumer<OppgaveIntern> {
        val kafkaProps = KafkaEmbed.consumerProps(env, EventType.OPPGAVE_INTERN)
        return setupConsumerForTheOppgaveTopic(kafkaProps, oppgaveEventProcessor, KafkaTestTopics.oppgaveInternTopicName)
    }

    private fun createInnboksConsumer(env: Environment, innboksEventProcessor: SimpleEventCounterService<InnboksIntern>): Consumer<InnboksIntern> {
        val kafkaProps = KafkaEmbed.consumerProps(env, EventType.INNBOKS_INTERN)
        return setupConsumerForTheInnboksTopic(kafkaProps, innboksEventProcessor, KafkaTestTopics.innboksInternTopicName)
    }

}
