package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldContainAll
import org.junit.jupiter.api.Test

internal class DbCountingMetricsSessionTest {

    val beskjederGruppertPerProdusent = mutableMapOf<String, Int>()
    val produsent1 = "produsent1"
    val produsent2 = "produsent2"
    val produsent3 = "produsent3"
    val produsent1Antall = 1
    val produsent2Antall = 2
    val produsent3Antall = 3
    val alleProdusenter = listOf(produsent1, produsent2, produsent3)

    init {
        beskjederGruppertPerProdusent[produsent1] = produsent1Antall
        beskjederGruppertPerProdusent[produsent2] = produsent2Antall
        beskjederGruppertPerProdusent[produsent3] = produsent3Antall
    }

    @Test
    fun `Skal telle opp riktig totalantall eventer, og rapportere riktig per produsent`() {
        val session = DbCountingMetricsSession(EventType.BESKJED)
        session.addEventsByProducer(beskjederGruppertPerProdusent)

        session.getTotalNumber() `should be equal to` (produsent1Antall + produsent2Antall + produsent3Antall)

        session.getProducers().size `should be equal to` alleProdusenter.size
        session.getProducers() shouldContainAll alleProdusenter

        session.getNumberOfEventsFor(produsent1) `should be equal to` produsent1Antall
        session.getNumberOfEventsFor(produsent2) `should be equal to` produsent2Antall
        session.getNumberOfEventsFor(produsent3) `should be equal to` produsent3Antall
    }

    @Test
    fun `Skal telle opp riktig totalantall eventer, hvis eventer legges til flere ganger`() {
        val session = DbCountingMetricsSession(EventType.BESKJED)
        session.addEventsByProducer(beskjederGruppertPerProdusent)
        session.addEventsByProducer(beskjederGruppertPerProdusent)

        session.getTotalNumber() `should be equal to` (produsent1Antall + produsent2Antall + produsent3Antall) * 2

        session.getProducers().size `should be equal to` alleProdusenter.size
        session.getProducers() shouldContainAll alleProdusenter

        session.getNumberOfEventsFor(produsent1) `should be equal to` produsent1Antall * 2
        session.getNumberOfEventsFor(produsent2) `should be equal to` produsent2Antall * 2
        session.getNumberOfEventsFor(produsent3) `should be equal to` produsent3Antall * 2
    }

}
