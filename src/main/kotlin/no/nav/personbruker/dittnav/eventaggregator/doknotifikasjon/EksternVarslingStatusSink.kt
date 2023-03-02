package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class EksternVarslingStatusSink(
    rapidsConnection: RapidsConnection,
    private val eksternVarslingStatusUpdater: EksternVarslingStatusUpdater
) :
    River.PacketListener {

    private val log: Logger = LoggerFactory.getLogger(EksternVarslingStatusSink::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "eksternVarslingStatus") }
            validate {
                it.requireKey(
                    "eventId",
                    "status",
                    "melding",
                    "kanaler",
                    "bestillerAppnavn"
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
            distribusjonsId = packet["distribusjonsId"].asLongOrNull(),
            kanaler = packet["kanaler"].map { it.asText() }
        )

        runBlocking {
            eksternVarslingStatusUpdater.insertOrUpdateStatus(eksternVarslingStatus)
            log.info("Behandlet eksternVarslingStatus fra rapid med eventid ${eksternVarslingStatus.eventId}")

            //TODO metricsProbe.countProcessed()
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }

    private fun JsonNode.asLongOrNull() = if (isNull) null else asLong()
}
