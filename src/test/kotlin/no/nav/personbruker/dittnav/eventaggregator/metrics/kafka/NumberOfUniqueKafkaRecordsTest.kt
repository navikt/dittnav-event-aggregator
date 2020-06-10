package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class NumberOfUniqueKafkaRecordsTest {

    @Test
    fun `Skal summere riktig`() {
        val kafkaRecords = NumberOfUniqueKafkaRecords(1, 2, 3, 4, 5, 6, 7, 8)
        kafkaRecords.totalt `should be equal to` 1 + 3 + 5 + 7
        kafkaRecords.beskjedTotal `should be equal to` 1 + 2
        kafkaRecords.innboksTotal `should be equal to` 3 + 4
        kafkaRecords.oppgaveTotal `should be equal to` 5 + 6
        kafkaRecords.doneTotal `should be equal to` 7 + 8
        kafkaRecords.eventsInTotal `should be equal to` 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8
    }

}
