package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternStatus.*
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHeader
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import java.time.Duration
import java.time.temporal.ChronoUnit

class EksternVarslingStatusUpdater(
    private val eksternVarslingStatusRepository: EksternVarslingStatusRepository,
    private val varselRepository: VarselRepository,
    private val eksternVarslingOppdatertProducer: EksternVarslingOppdatertProducer
) {

    suspend fun insertOrUpdateStatus(statusEvent: DoknotifikasjonStatusEvent) {
        val varsel = varselRepository.getVarsel(statusEvent.eventId)

        if (varsel == null) {
            return
        }

        val currentStatus = eksternVarslingStatusRepository.getStatusIfExists(statusEvent.eventId, varsel.type)

        if (currentStatus == null) {
            insertNewStatus(statusEvent, varsel)
        } else if (hasNotReceivedSameStatus(currentStatus, statusEvent)) {
            updateExistingStatus(statusEvent, currentStatus, varsel)
        }
    }

    private suspend fun insertNewStatus(statusEvent: DoknotifikasjonStatusEvent, varsel: VarselHeader) {
        val newEntry = EksternVarslingHistorikkEntry(
            melding = statusEvent.melding,
            status = determineInternalStatus(statusEvent),
            distribusjonsId = statusEvent.distribusjonsId,
            kanal = statusEvent.kanal,
            renotifikasjon = false,
            tidspunkt = statusEvent.tidspunkt
        )

        val newStatus = EksternVarslingStatus(
            eventId = statusEvent.eventId,
            eksternVarslingSendt = newEntry.status == Sendt,
            renotifikasjonSendt = false,
            kanaler = listOfNotNull(newEntry.kanal),
            sistMottattStatus = statusEvent.status,
            historikk = listOf(newEntry),
            sistOppdatert = nowAtUtc()
        )

        eksternVarslingStatusRepository.updateStatus(newStatus, varsel.type)

        eksternVarslingOppdatertProducer.eksternStatusOppdatert(buildOppdatering(newEntry, varsel))
    }

    private suspend fun updateExistingStatus(statusEvent: DoknotifikasjonStatusEvent, currentStatus: EksternVarslingStatus, varsel: VarselHeader) {
        val newEntry = EksternVarslingHistorikkEntry(
            melding = statusEvent.melding,
            status = determineInternalStatus(statusEvent),
            distribusjonsId = statusEvent.distribusjonsId,
            kanal = statusEvent.kanal,
            renotifikasjon = determineIfRenotifikasjon(currentStatus, statusEvent),
            tidspunkt = statusEvent.tidspunkt
        )

        val updatedStatus = EksternVarslingStatus(
            eventId = currentStatus.eventId,
            eksternVarslingSendt = currentStatus.eksternVarslingSendt || newEntry.status == Sendt,
            renotifikasjonSendt = if (newEntry.renotifikasjon == true) true else currentStatus.renotifikasjonSendt,
            kanaler = (currentStatus.kanaler + newEntry.kanal).filterNotNull().distinct(),
            sistMottattStatus = statusEvent.status,
            historikk = currentStatus.historikk + newEntry,
            sistOppdatert = nowAtUtc()
        )

        eksternVarslingStatusRepository.updateStatus(updatedStatus, varsel.type)

        eksternVarslingOppdatertProducer.eksternStatusOppdatert(buildOppdatering(newEntry, varsel))
    }

    private fun determineIfRenotifikasjon(currentStatus: EksternVarslingStatus, statusEvent: DoknotifikasjonStatusEvent): Boolean {
        return when {
            determineInternalStatus(statusEvent) != Sendt -> false
            isFirstAttempt(currentStatus) -> false
            intervalSinceFirstAttempt(currentStatus, statusEvent) > Duration.ofHours(23) -> true
            else -> false
        }
    }

    private fun isFirstAttempt(currentStatus: EksternVarslingStatus): Boolean {
        return currentStatus.historikk.none { it.status == Sendt || it.status == Feilet }
    }

    private fun hasNotReceivedSameStatus(currentStatus: EksternVarslingStatus, statusEvent: DoknotifikasjonStatusEvent): Boolean {
        return currentStatus.historikk
            .filter { it.status == determineInternalStatus(statusEvent) }
            .filter { it.distribusjonsId == statusEvent.distribusjonsId }
            .filter { it.tidspunkt.truncatedTo(ChronoUnit.MILLIS) == statusEvent.tidspunkt.truncatedTo(ChronoUnit.MILLIS) }
            .none()
    }

    private fun intervalSinceFirstAttempt(currentStatus: EksternVarslingStatus, statusEvent: DoknotifikasjonStatusEvent): Duration {
        val previous = currentStatus.historikk
            .filter { it.status == Sendt || it.status == Feilet }
            .minOf { it.tidspunkt }

        return Duration.between(previous, statusEvent.tidspunkt)
    }

    private fun buildOppdatering(newEntry: EksternVarslingHistorikkEntry, varsel: VarselHeader) = EksternStatusOppdatering(
        status = newEntry.status,
        kanal = newEntry.kanal,
        varselType = varsel.type,
        eventId = varsel.eventId,
        ident = varsel.fodselsnummer,
        namespace = varsel.namespace,
        appnavn = varsel.appnavn,
        renotifikasjon = newEntry.renotifikasjon
    )

    private fun determineInternalStatus(statusEvent: DoknotifikasjonStatusEvent): EksternStatus {
        return when(statusEvent.status) {
            FERDIGSTILT.name -> if (statusEvent.kanal.isNullOrBlank()) Ferdigstilt else Sendt
            INFO.name -> Info
            FEILET.name -> Feilet
            OVERSENDT.name -> Bestilt
            else -> throw IllegalArgumentException("Kjente ikke igjen doknotifikasjon status ${statusEvent.status}.")
        }
    }
}
