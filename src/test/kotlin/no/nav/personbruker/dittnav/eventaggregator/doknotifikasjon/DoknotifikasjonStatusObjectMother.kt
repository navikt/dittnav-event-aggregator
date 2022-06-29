package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus

object DoknotifikasjonStatusObjectMother {

    private val defaultBestiller = "dummyBestiller"
    private val defaultStatus = "INFO"
    private val defaultMelding = "dummyMelding"
    private val defaultDistribusjonsId = 1L

    fun createDoknotifikasjonStatus(
        bestillingsId: String,
        bestiller: String = defaultBestiller,
        status: String = defaultStatus,
        melding: String = defaultMelding,
        distribusjonsId: Long = defaultDistribusjonsId
    ): DoknotifikasjonStatus {
        return DoknotifikasjonStatus.newBuilder()
                .setBestillingsId(bestillingsId)
                .setBestillerId(bestiller)
                .setStatus(status)
                .setMelding(melding)
                .setDistribusjonId(distribusjonsId)
                .build()
    }

}
