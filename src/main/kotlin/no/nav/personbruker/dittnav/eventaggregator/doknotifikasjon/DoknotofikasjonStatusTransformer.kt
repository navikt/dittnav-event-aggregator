package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus

object DoknotofikasjonStatusTransformer {
    fun toInternal(external: DoknotifikasjonStatus): DoknotifikasjonStatusDto {
        return DoknotifikasjonStatusDto(
            eventId = external.getBestillingsId(),
            bestillerAppnavn = external.getBestillerId(),
            status = external.getStatus(),
            melding = external.getMelding(),
            distribusjonsId = external.getDistribusjonId(),
            kanaler = getKanaler(external)
        )
    }

    private fun getKanaler(external: DoknotifikasjonStatus): List<String> {
        val kanal = parseKanal(external.getMelding())

        return if (kanal != null) {
            listOf(kanal)
        } else {
            emptyList()
        }
    }

    private val kanalMeldingPattern = "notifikasjon sendt via (\\w+)".toRegex()

    private fun parseKanal(melding: String): String? {
        return kanalMeldingPattern.find(melding)?.destructured?.let { (kanal) ->
            kanal.uppercase()
        }
    }
}
