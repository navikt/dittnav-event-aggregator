package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

class KafkaEventCounterService(val environment: Environment) {

    private val log = LoggerFactory.getLogger(KafkaEventCounterService::class.java)

    private val beskjedConsumer = createCountConsumer<Beskjed>(EventType.BESKJED, Kafka.beskjedTopicName, environment)
    private val oppgaveConsumer = createCountConsumer<Oppgave>(EventType.OPPGAVE, Kafka.oppgaveTopicName, environment)
    private val innboksConsumer = createCountConsumer<Innboks>(EventType.INNBOKS, Kafka.innboksTopicName, environment)
    private val doneConsumer = createCountConsumer<Done>(EventType.DONE, Kafka.doneTopicName, environment)

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

    fun countUniqueEvents(): NumberOfUniqueKafkaRecords {
        val beskjedPair = countUniqueBeskjeder()
        val innboksPair = countUniqueInnboksEventer()
        val oppgaverPair = countUniqueOppgaver()
        val donePair = countUniqueDoneEvents()
        val result = NumberOfUniqueKafkaRecords(
                beskjed = beskjedPair.first,
                beskjedDuplicates = beskjedPair.second,
                innboks = innboksPair.first,
                innboksDuplicates = innboksPair.second,
                oppgaver = oppgaverPair.first,
                oppgaverDuplicates = oppgaverPair.second,
                done = donePair.first,
                doneDuplicates = donePair.second
        )

        log.info("Fant følgende unike eventer:\n$result")
        return result
    }

    fun countUniqueBeskjeder(): Pair<Long, Long> {
        return try {
            countUniqueEvents(beskjedConsumer as KafkaConsumer<Nokkel, GenericRecord>, EventType.BESKJED)
        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall beskjed-eventer", e)
            Pair(-1, -1)
        }
    }

    fun countUniqueInnboksEventer(): Pair<Long, Long> {
        return if (isOtherEnvironmentThanProd()) {
            try {
                countUniqueEvents(innboksConsumer as KafkaConsumer<Nokkel, GenericRecord>, EventType.INNBOKS)
            } catch (e: Exception) {
                log.warn("Klarte ikke å telle antall innboks-eventer", e)
                Pair(-1L, -1L)
            }
        } else {
            Pair(0L, 0L)
        }
    }

    fun countUniqueOppgaver(): Pair<Long, Long> {
        return try {
            countUniqueEvents(oppgaveConsumer as KafkaConsumer<Nokkel, GenericRecord>, EventType.OPPGAVE)
        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall oppgave-eventer", e)
            Pair(-1, -1)
        }
    }

    fun countUniqueDoneEvents(): Pair<Long, Long> {
        return try {
            countUniqueEvents(doneConsumer as KafkaConsumer<Nokkel, GenericRecord>, EventType.DONE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            Pair(-1, -1)
        }
    }

    fun closeAllConsumers() {
        closeConsumer(beskjedConsumer)
        closeConsumer(innboksConsumer)
        closeConsumer(oppgaveConsumer)
        closeConsumer(doneConsumer)
    }
}
