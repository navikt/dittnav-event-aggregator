package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.ZoneId

class StatusoppdateringTransformerTest {

    @Test
    fun `should transform form external to internal`() {
        val eventId = 1
        val original = AvroStatusoppdateringObjectMother.createStatusoppdatering(eventId)
        val nokkel = createNokkel(eventId)

        val transformed = StatusoppdateringTransformer.toInternal(nokkel, original)

        transformed.fodselsnummer `should be equal to` nokkel.getFodselsnummer()
        transformed.grupperingsId `should be equal to` original.getGrupperingsId()
        transformed.eventId `should be equal to` nokkel.getEventId()
        transformed.link `should be equal to` original.getLink()
        transformed.systembruker `should be equal to` nokkel.getSystembruker()
        transformed.sikkerhetsnivaa `should be equal to` original.getSikkerhetsnivaa()
        transformed.statusGlobal `should be equal to` original.getStatusGlobal()
        transformed.statusIntern?.`should be equal to`(original.getStatusIntern())
        transformed.sakstema `should be equal to` original.getSakstema()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()

        transformed.sistOppdatert.`should not be null`()
        transformed.id.`should be null`()
    }

}
