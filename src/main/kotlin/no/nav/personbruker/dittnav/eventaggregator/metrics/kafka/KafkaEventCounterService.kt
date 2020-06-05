package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Innboks
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd
import org.slf4j.LoggerFactory

class KafkaEventCounterService(val environment: Environment) {

    private val log = LoggerFactory.getLogger(KafkaEventCounterService::class.java)

    private var beskjedConsumer = createCountConsumer<Beskjed>(EventType.BESKJED, Kafka.beskjedTopicName, environment)
    private var oppgaveConsumer = createCountConsumer<Oppgave>(EventType.OPPGAVE, Kafka.oppgaveTopicName, environment)
    private var innboksConsumer = createCountConsumer<Innboks>(EventType.INNBOKS, Kafka.innboksTopicName, environment)
    private var doneConsumer = createCountConsumer<Done>(EventType.DONE, Kafka.doneTopicName, environment)

    fun countAllEvents(): NumberOfKafkaRecords {
        val result = NumberOfKafkaRecords(
                beskjed = countBeskjeder(),
                innboks = countInnboksEventer(),
                oppgaver = countOppgaver(),
                done = countDoneEvents()
        )

        log.info("Fant følgende eventer:\n$result")
        return result
    }

    fun countBeskjeder(): Long {
        return try {
            countEvents(beskjedConsumer, EventType.BESKJED)
        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall beskjed-eventer", e)
            -1
        }
    }

    fun countInnboksEventer(): Long {
        return if (isOtherEnvironmentThanProd()) {
            try {
                countEvents(innboksConsumer, EventType.INNBOKS)
            } catch (e: Exception) {
                log.warn("Klarte ikke å telle antall innboks-eventer", e)
                -1L
            }
        } else {
            0
        }
    }

    fun countOppgaver(): Long {
        return try {
            countEvents(oppgaveConsumer, EventType.OPPGAVE)
        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall oppgave-eventer", e)
            -1
        }
    }

    fun countDoneEvents(): Long {
        return try {
            countEvents(doneConsumer, EventType.DONE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            -1
        }
    }

    fun closeAllConsumers() {
        closeConsumer(beskjedConsumer)
        closeConsumer(innboksConsumer)
        closeConsumer(oppgaveConsumer)
        closeConsumer(doneConsumer)
    }
}
