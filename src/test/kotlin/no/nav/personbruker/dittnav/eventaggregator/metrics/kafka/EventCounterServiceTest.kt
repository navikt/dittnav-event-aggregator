package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.personbruker.dittnav.eventaggregator.config.Environment

internal class EventCounterServiceTest {

    //    @Test // For kjøring lokalt
    fun countEvents() {
        val counterService = EventCounterService(Environment())

        val result = counterService.countEvents()

        println(result)
    }

}
