package no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class EksternVarslingStatusSink(
    rapidsConnection: RapidsConnection,
    private val varselRepository: VarselRepository,
    private val doknotifikasjonStatusRepository: DoknotifikasjonStatusRepository,
    private val writeToDb: Boolean
) :
    River.PacketListener {

    private val log: Logger = LoggerFactory.getLogger(EksternVarslingStatusSink::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "eksternVarslingStatus") }
            validate {
                it.requireKey(
                    "eventId",
                    "bestillerAppnavn",
                    "status",
                    "melding",
                    "kanaler"
                )
            }
            validate { it.interestedIn("distribusjonsId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eksternVarslingStatus = DoknotifikasjonStatusDto(
            eventId = packet["eventId"].asText(),
            bestillerAppnavn = packet["bestillerAppnavn"].asText(),
            status = packet["status"].asText(),
            melding = packet["melding"].asText(),
            distribusjonsId = packet["distribusjonsId"].asLong(),
            kanaler = packet["kanaler"].map { it.asText() },
        )

        runBlocking {
            val varsel = varselRepository.getBrukernotifikasjoner(eksternVarslingStatus.eventId)

            if (writeToDb) {
                if (varsel.isNotEmpty()) {
                    insertOrUpdateStatus(varsel.first().type, eksternVarslingStatus)
                }
            } else {
                log.info("Dryrun: eksternVarslingStatus fra rapid med eventid ${eksternVarslingStatus.eventId}")
            }

            //TODO metricsProbe.countProcessed()
        }
    }

    private suspend fun insertOrUpdateStatus(eventType: EventType, newStatus: DoknotifikasjonStatusDto) {
        if (eventType == EventType.BESKJED_INTERN) {
            val existingStatus = doknotifikasjonStatusRepository.getStatusesForBeskjed(listOf(newStatus.eventId))

            val statusToPersist =
                if(existingStatus.isEmpty()) newStatus
                else mergeStatuses(existingStatus.first(), newStatus)

            doknotifikasjonStatusRepository.updateStatusesForBeskjed(listOf(statusToPersist))
        } else if (eventType == EventType.OPPGAVE_INTERN) {
            val existingStatus = doknotifikasjonStatusRepository.getStatusesForOppgave(listOf(newStatus.eventId))

            val statusToPersist =
                if(existingStatus.isEmpty()) newStatus
                else mergeStatuses(existingStatus.first(), newStatus)

            doknotifikasjonStatusRepository.updateStatusesForOppgave(listOf(statusToPersist))
        } else if (eventType == EventType.INNBOKS_INTERN) {
            val existingStatus = doknotifikasjonStatusRepository.getStatusesForInnboks(listOf(newStatus.eventId))

            val statusToPersist =
                if(existingStatus.isEmpty()) newStatus
                else mergeStatuses(existingStatus.first(), newStatus)

            doknotifikasjonStatusRepository.updateStatusesForInnboks(listOf(statusToPersist))
        }
    }

    private fun mergeStatuses(oldStatus: DoknotifikasjonStatusDto, newStatus: DoknotifikasjonStatusDto): DoknotifikasjonStatusDto {
        val kanaler = (oldStatus.kanaler + newStatus.kanaler).distinct()

        return newStatus.copy(
            kanaler = kanaler,
            antallOppdateringer = oldStatus.antallOppdateringer + 1
        )
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }
}