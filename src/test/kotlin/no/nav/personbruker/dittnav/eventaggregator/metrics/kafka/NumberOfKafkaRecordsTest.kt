package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class NumberOfKafkaRecordsTest {

    @Test
    fun `Skal summere totalen riktig`() {
        val kafkaRecords = NumberOfKafkaRecords(1, 2, 3, 4)
        kafkaRecords.totalt `should be equal to` 1 + 2 + 3 + 4
    }

}
