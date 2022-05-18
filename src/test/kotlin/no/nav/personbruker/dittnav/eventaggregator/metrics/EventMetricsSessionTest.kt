package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.junit.jupiter.api.Test

internal class EventMetricsSessionTest {

    @Test
    fun `Skal returnere true hvis det kun er fpinfo-historik som har sendt duplikat`() {
        val session = EventMetricsSession(EventType.BESKJED_INTERN)
        session.countDuplicateEventKeysByProducer(Produsent("blalbafpinfo-historikk", "dummyNamespace"))

        session.isDuplicatesFromFpinfoHistorikkOnly() shouldBe true
    }

    @Test
    fun `Skal returnere false hvis det er flere produsenter, inkludert fpinfo-historik, som har sendt duplikater`() {
        val session = EventMetricsSession(EventType.BESKJED_INTERN)
        session.countDuplicateEventKeysByProducer(Produsent("blablafpinfo-historikk", "dummyNamespace"))
        session.countDuplicateEventKeysByProducer(Produsent("enAnnenProdusent", "dummyNamespace"))

        session.isDuplicatesFromFpinfoHistorikkOnly() shouldBe false
    }

    @Test
    fun `Skal returnere false hvis det er en annen produsent enn fpinfo-historik som har sendt duplikat`() {
        val session = EventMetricsSession(EventType.BESKJED_INTERN)
        session.countDuplicateEventKeysByProducer(Produsent("enAnnenProdusent", "dummyNamespace"))

        session.isDuplicatesFromFpinfoHistorikkOnly() shouldBe false
    }

}
