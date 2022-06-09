package no.nav.personbruker.dittnav.eventaggregator.nokkel

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import java.util.UUID

object AvroNokkelInternObjectMother {

        private val defaultUlid = "dummyUlid"
        private val defaultEventId = UUID.randomUUID().toString()
        private val defaultGrupperingsid = "dummyGrupperingsid"
        private val defaultFodselsnummer = "12345"
        private val defaultNamespace = "dummyNamespace"
        private val defaultAppnavn = "dummyAppnavn"
        private val defaultSystembruker = "systembruker"

        fun createNokkelWithEventId(eventId: Int): NokkelIntern {
                return NokkelIntern(
                        defaultUlid,
                        eventId.toString(),
                        defaultGrupperingsid,
                        defaultFodselsnummer,
                        defaultNamespace,
                        defaultAppnavn,
                        defaultSystembruker
                );
        }

        fun createNokkel(grupperingsid: String, fodselsnummer: String, namespace: String, appnavn: String): NokkelIntern {
                return NokkelIntern(
                        defaultUlid,
                        defaultEventId,
                        grupperingsid,
                        fodselsnummer,
                        namespace,
                        appnavn,
                        defaultSystembruker
                )
        }

}

