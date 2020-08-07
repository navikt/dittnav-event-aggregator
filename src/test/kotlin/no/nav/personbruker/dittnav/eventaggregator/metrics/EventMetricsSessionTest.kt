package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test

internal class EventMetricsSessionTest {

    @Test
    fun `Skal returnere true hvis det kun er fpinfo-historik som har sendt duplikat`() {
        val session = EventMetricsSession(EventType.BESKJED)
        session.countDuplicateEventKeysByProducer("blalbafpinfo-historikk")

        session.isDuplicatesFromFpinfoHistorikkOnly() `should be` true
    }

    @Test
    fun `Skal returnere false hvis det er flere produsenter, inkludert fpinfo-historik, som har sendt duplikater`() {
        val session = EventMetricsSession(EventType.BESKJED)
        session.countDuplicateEventKeysByProducer("blablafpinfo-historikk")
        session.countDuplicateEventKeysByProducer("enAnnenProdusent")

        session.isDuplicatesFromFpinfoHistorikkOnly() `should be` false
    }

    @Test
    fun `Skal returnere false hvis det er en annen produsent enn fpinfo-historik som har sendt duplikat`() {
        val session = EventMetricsSession(EventType.BESKJED)
        session.countDuplicateEventKeysByProducer("enAnnenProdusent")

        session.isDuplicatesFromFpinfoHistorikkOnly() `should be` false
    }

}
