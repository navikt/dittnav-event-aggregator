package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

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
    private val eksternVarslingStatusUpdater: EksternVarslingStatusUpdater,
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
            bestillerAppnavn = "", //Ubrukt, burde fjernes fra klasse
            status = packet["status"].asText(),
            melding = packet["melding"].asText(),
            distribusjonsId = packet["distribusjonsId"].asLong(),
            kanaler = packet["kanaler"].map { it.asText() },
        )

        runBlocking {
            if (writeToDb) {
                eksternVarslingStatusUpdater.insertOrUpdateStatus(eksternVarslingStatus)
                log.info("Behandlet eksternVarslingStatus fra rapid med eventid ${eksternVarslingStatus.eventId}")
            }

            //TODO metricsProbe.countProcessed()
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }
}