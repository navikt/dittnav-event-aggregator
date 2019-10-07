package no.nav.personbruker.dittnav.eventaggregator.kafka.consumer

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.brukernotifikasjon.schemas.Melding
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.config.*
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheInformasjonTopic
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheMeldingTopic
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheOppgaveTopic
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.MeldingObjectMother
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.service.impl.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.util.KafkaTestUtil
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class MultipleTopicsConsumerTest {

    private val topics = listOf(Kafka.informasjonTopicName, Kafka.oppgaveTopicName, Kafka.meldingTopicName)
    private val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(topics)
    private val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)
    private val adminClient = embeddedEnv.adminClient

    private val informasjonEventProcessor = SimpleEventCounterService<Informasjon>()
    private val oppgaveEventProcessor = SimpleEventCounterService<Oppgave>()
    private val meldingEventProcessor = SimpleEventCounterService<Melding>()

    private val informasjonEvents = (1..10).map { "$it" to InformasjonObjectMother.createInformasjon(it) }.toMap()
    private val oppgaveEvents = (1..11).map { "$it" to OppgaveObjectMother.createOppgave(it) }.toMap()
    private val meldingEvents = (1..12).map { "$it" to MeldingObjectMother.createMelding(it) }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun tearDown() {
        adminClient?.close()
        embeddedEnv.tearDown()
    }

    @Test
    fun `Kafka instansen i minnet har blitt staret`() {
        embeddedEnv.serverPark.status `should equal` KafkaEnvironment.ServerParkStatus.Started
    }

    fun `Produserer testeventer for alle topics`() {
        runBlocking {
            val producedAllInformasjon = KafkaTestUtil.produceEvents(testEnvironment, Kafka.informasjonTopicName, informasjonEvents)
            val producedAllOppgave = KafkaTestUtil.produceEvents(testEnvironment, Kafka.oppgaveTopicName, oppgaveEvents)
            val producedAllMelding = KafkaTestUtil.produceEvents(testEnvironment, Kafka.meldingTopicName, meldingEvents)
            producedAllInformasjon `should be equal to` true
            producedAllOppgave `should be equal to` true
            producedAllMelding `should be equal to` true
        }
    }

    @Test
    fun `Skal kunne konsumere fra flere topics i parallell`() {
        `Produserer testeventer for alle topics`()

        val informasjonConsumer = createInfoConsumer(testEnvironment, informasjonEventProcessor)
        val oppgaveConsumer = createOppgaveConsumer(testEnvironment, oppgaveEventProcessor)
        val meldingConsumer = createMeldingConsumer(testEnvironment, meldingEventProcessor)

        runBlocking {
            informasjonConsumer.poll()
            oppgaveConsumer.poll()
            meldingConsumer.poll()

            `Vent til alle eventer har blitt konsumert`()

            informasjonConsumer.cancel()
            oppgaveConsumer.cancel()
            meldingConsumer.cancel()

            informasjonEvents.size `should be equal to` informasjonEventProcessor.eventCounter
            oppgaveEvents.size `should be equal to` oppgaveEventProcessor.eventCounter
            meldingEvents.size `should be equal to`  meldingEventProcessor.eventCounter
        }
    }

    private suspend fun `Vent til alle eventer har blitt konsumert`() {
        while (`Har alle eventer blitt lest`(informasjonEventProcessor, informasjonEvents,
                        oppgaveEventProcessor, oppgaveEvents,
                        meldingEventProcessor, meldingEvents)) {
            delay(100)
        }
    }

    private fun `Har alle eventer blitt lest`(informasjon: SimpleEventCounterService<Informasjon>, informasjonEvents: Map<String, Informasjon>,
                                              oppgave: SimpleEventCounterService<Oppgave>, oppgaveEvents: Map<String, Oppgave>,
                                              melding: SimpleEventCounterService<Melding>, meldingEvents: Map<String, Melding>): Boolean {
        return informasjon.eventCounter < informasjonEvents.size &&
                oppgave.eventCounter < oppgaveEvents.size &&
                melding.eventCounter < meldingEvents.size
    }

    private fun createInfoConsumer(env: Environment, informasjonEventProcessor: SimpleEventCounterService<Informasjon>): Consumer<Informasjon> {
        val kafkaProps = Kafka.consumerProps(env, EventType.INFORMASJON, true)
        return setupConsumerForTheInformasjonTopic(kafkaProps, informasjonEventProcessor)
    }

    private fun createOppgaveConsumer(env: Environment, oppgaveEventProcessor: SimpleEventCounterService<Oppgave>): Consumer<Oppgave> {
        val kafkaProps = Kafka.consumerProps(env, EventType.OPPGAVE, true)
        return setupConsumerForTheOppgaveTopic(kafkaProps, oppgaveEventProcessor)
    }

    private fun createMeldingConsumer(env: Environment, meldingEventProcessor: SimpleEventCounterService<Melding>): Consumer<Melding> {
        val kafkaProps = Kafka.consumerProps(env, EventType.MELDING, true)
        return setupConsumerForTheMeldingTopic(kafkaProps, meldingEventProcessor)
    }

}
