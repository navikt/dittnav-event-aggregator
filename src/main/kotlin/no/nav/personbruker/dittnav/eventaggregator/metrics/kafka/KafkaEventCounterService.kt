package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

class KafkaEventCounterService(
        val beskjedCountConsumer: KafkaConsumer<Nokkel, GenericRecord>,
        val innboksCountConsumer: KafkaConsumer<Nokkel, GenericRecord>,
        val oppgaveCountConsumer: KafkaConsumer<Nokkel, GenericRecord>,
        val doneCountConsumer: KafkaConsumer<Nokkel, GenericRecord>
) {

    private val log = LoggerFactory.getLogger(KafkaEventCounterService::class.java)

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
            countEvents(beskjedCountConsumer, EventType.BESKJED)
        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall beskjed-eventer", e)
            -1
        }
    }

    fun countInnboksEventer(): Long {
        return if (isOtherEnvironmentThanProd()) {
            try {
                countEvents(innboksCountConsumer, EventType.INNBOKS)
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
            countEvents(oppgaveCountConsumer, EventType.OPPGAVE)
        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall oppgave-eventer", e)
            -1
        }
    }

    fun countDoneEvents(): Long {
        return try {
            countEvents(doneCountConsumer, EventType.DONE)

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
            countUniqueEvents(beskjedCountConsumer, EventType.BESKJED)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall beskjed-eventer", e)
            Pair(-1, -1)
        }
    }

    fun countUniqueInnboksEventer(): Pair<Long, Long> {
        return if (isOtherEnvironmentThanProd()) {
            try {
                countUniqueEvents(innboksCountConsumer, EventType.INNBOKS)

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
            countUniqueEvents(oppgaveCountConsumer, EventType.OPPGAVE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall oppgave-eventer", e)
            Pair(-1, -1)
        }
    }

    fun countUniqueDoneEvents(): Pair<Long, Long> {
        return try {
            countUniqueEvents(doneCountConsumer, EventType.DONE)


        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            Pair(-1, -1)
        }
    }

}
