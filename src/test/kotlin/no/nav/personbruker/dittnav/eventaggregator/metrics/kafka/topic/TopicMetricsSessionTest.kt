package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.UniqueKafkaEventIdentifier
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class TopicMetricsSessionTest {

    @Test
    fun `Skal summere opp eventer riktig i hver kategori per produsent`() {
        val metricsSession = TopicMetricsSession(EventType.BESKJED)
        val produsent1 = "p-1"
        val produsent2 = "p-2"
        val produsent3 = "p-3"
        val produsent1Event1 = UniqueKafkaEventIdentifier("b-1", produsent1, "1")
        val produsent2Event1 = UniqueKafkaEventIdentifier("b-11", produsent2, "2")
        val produsent2Event2 = UniqueKafkaEventIdentifier("b-12", produsent2, "2")
        val produsent3Event1 = UniqueKafkaEventIdentifier("b-21", produsent3, "3")
        val produsent3Event2 = UniqueKafkaEventIdentifier("b-22", produsent3, "3")
        val produsent3Event3 = UniqueKafkaEventIdentifier("b-23", produsent3, "3")


        metricsSession.countEvent(produsent1Event1)
        metricsSession.countEvent(produsent1Event1)
        metricsSession.countEvent(produsent1Event1)
        metricsSession.countEvent(produsent1Event1)

        metricsSession.countEvent(produsent2Event1)
        metricsSession.countEvent(produsent2Event2)
        metricsSession.countEvent(produsent2Event2)

        metricsSession.countEvent(produsent3Event1)
        metricsSession.countEvent(produsent3Event2)
        metricsSession.countEvent(produsent3Event3)
        metricsSession.countEvent(produsent3Event3)
        metricsSession.countEvent(produsent3Event3)


        metricsSession.getDuplicates() `should be equal to` 3 + 1 + 2
        metricsSession.getDuplicates(produsent1) `should be equal to` 3
        metricsSession.getDuplicates(produsent2) `should be equal to` 1
        metricsSession.getDuplicates(produsent3) `should be equal to` 2

        metricsSession.getTotalNumber() `should be equal to` 4 + 3 + 5
        metricsSession.getTotalNumber(produsent1) `should be equal to` 4
        metricsSession.getTotalNumber(produsent2) `should be equal to` 3
        metricsSession.getTotalNumber(produsent3) `should be equal to` 5

        metricsSession.getNumberOfUniqueEvents() `should be equal to` 1 + 2 + 3
        metricsSession.getNumberOfUniqueEvents(produsent1) `should be equal to` 1
        metricsSession.getNumberOfUniqueEvents(produsent2) `should be equal to` 2
        metricsSession.getNumberOfUniqueEvents(produsent3) `should be equal to` 3
    }

}
