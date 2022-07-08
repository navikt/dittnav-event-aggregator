package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus

object DoknotifikasjonStatusDtoObjectMother {

    private val defaultBestillerAppnavn = "dummyBestiller"
    private val defaultStatus = "INFO"
    private val defaultMelding = "dummyMelding"
    private val defaultDistribusjonsId = 1L
    private val defaultKanaler = emptyList<String>()
    private val defaultAntallOppdateringer = 1

    fun createDoknotifikasjonStatusDto(
        eventId: String,
        bestillerAppnavn: String = defaultBestillerAppnavn,
        status: String = defaultStatus,
        melding: String = defaultMelding,
        distribusjonsId: Long = defaultDistribusjonsId,
        kanaler: List<String> = defaultKanaler,
        antallOppdateringer: Int = defaultAntallOppdateringer
    ): DoknotifikasjonStatusDto {
        return DoknotifikasjonStatusDto(
            eventId = eventId,
            bestillerAppnavn = bestillerAppnavn,
            status = status,
            melding = melding,
            distribusjonsId = distribusjonsId,
            kanaler = kanaler,
            antallOppdateringer = antallOppdateringer,
        )
    }
}

