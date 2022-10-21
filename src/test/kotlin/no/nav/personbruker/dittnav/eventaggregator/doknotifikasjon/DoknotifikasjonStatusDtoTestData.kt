package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

object DoknotifikasjonStatusDtoTestData {

    fun createDoknotifikasjonStatusDto(
        eventId: String,
        bestillerAppnavn: String = "dummyBestiller",
        status: String =  "INFO",
        melding: String = "dummyMelding",
        distribusjonsId: Long = 1L,
        kanaler: List<String> = emptyList(),
        antallOppdateringer: Int =  1
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

