package no.nav.personbruker.dittnav.eventaggregator.varsel

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.asOptionalLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

internal class BeskjedSink(rapidsConnection: RapidsConnection, private val beskjedRepository: BeskjedRepository) :
    River.PacketListener {

    private val log: Logger = LoggerFactory.getLogger(BeskjedSink::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "beskjed") }
            validate { it.demandValue("aktiv", true) }
            validate { it.requireKey(
                "namespace",
                "appnavn",
                "eventId",
                "forstBehandlet",
                "fodselsnummer",
                "tekst",
                "link",
                "sikkerhetsnivaa",
                "eksternVarsling"
            ) }
            validate { it.interestedIn("synligFremTil", "prefererteKanaler")}
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val beskjed = Beskjed(
            id = null,
            systembruker = "N/A",
            namespace = packet["namespace"].textValue(),
            appnavn = packet["appnavn"].textValue(),
            eventId = packet["eventId"].textValue(),
            eventTidspunkt = packet["forstBehandlet"].asLocalDateTime(), //Felt ikke i bruk
            forstBehandlet = packet["forstBehandlet"].asLocalDateTime(),
            fodselsnummer = packet["fodselsnummer"].textValue(),
            grupperingsId = "N/A",
            tekst = packet["tekst"].textValue(),
            link = packet["link"].textValue(),
            sikkerhetsnivaa = packet["sikkerhetsnivaa"].intValue(),
            sistOppdatert = nowAtUtc(),
            synligFremTil = packet["synligFremTil"].asOptionalLocalDateTime(),
            aktiv = packet["aktiv"].booleanValue(),
            eksternVarsling = packet["eksternVarsling"].booleanValue(),
            prefererteKanaler = packet["prefererteKanaler"].map { it.textValue() }
        )

        runBlocking {
            beskjedRepository.createInOneBatch(listOf(beskjed))
            log.info("Behandlet beskjed fra rapid med eventid ${beskjed.eventId}")
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }

    //TODO: Fjern, bare for debugging
    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        log.warn(error.toString())
    }
}
