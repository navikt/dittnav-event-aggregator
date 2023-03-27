package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*

internal class EksternVarslingStatusSink(
    rapidsConnection: RapidsConnection,
    private val eksternVarslingStatusUpdater: EksternVarslingStatusUpdater
) :
    River.PacketListener {

    private val log = KotlinLogging.logger {  }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "eksternVarslingStatus") }
            validate {
                it.requireKey(
                    "eventId",
                    "status",
                    "melding",
                    "bestillerAppnavn",
                    "tidspunkt"
                )
            }
            validate { it.interestedIn("distribusjonsId", "kanal") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eksternVarslingStatus = DoknotifikasjonStatusEvent(
            eventId = packet["eventId"].asText(),
            bestillerAppnavn = packet["bestillerAppnavn"].asText(),
            status = packet["status"].asText(),
            melding = packet["melding"].asText(),
            distribusjonsId = packet["distribusjonsId"].asLongOrNull(),
            kanal = packet["kanal"].asTextOrNull(),
            tidspunkt = packet["tidspunkt"].asLocalDateTime()
        )

        runBlocking {
            eksternVarslingStatusUpdater.insertOrUpdateStatus(eksternVarslingStatus)
            log.info("Behandlet eksternVarslingStatus fra rapid med eventid ${eksternVarslingStatus.eventId}")
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }

    private fun JsonNode.asLongOrNull() = if (isNull) null else asLong()

    private fun JsonNode.asTextOrNull() = if (isNull) null else asText()
}
