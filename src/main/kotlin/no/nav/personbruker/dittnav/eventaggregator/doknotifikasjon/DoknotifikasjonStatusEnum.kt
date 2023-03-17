package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternStatus.*

enum class DoknotifikasjonStatusEnum {
    FEILET, INFO, OVERSENDT, FERDIGSTILT;

    companion object {
        fun fromInternal(status: EksternStatus) = when(status) {
            Feilet -> FEILET
            Info -> INFO
            Bestilt -> OVERSENDT
            Sendt -> FERDIGSTILT
            Ferdigstilt -> FERDIGSTILT
        }
    }
}
