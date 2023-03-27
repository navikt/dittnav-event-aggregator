package no.nav.personbruker.dittnav.eventaggregator.innboks

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.RapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselAktivertProducer
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository

internal class InnboksSink(
    rapidsConnection: RapidsConnection,
    private val varselRepository: VarselRepository,
    private val varselAktivertProducer: VarselAktivertProducer,
    private val rapidMetricsProbe: RapidMetricsProbe
) :
    River.PacketListener {

    private val log = KotlinLogging.logger { }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "innboks") }
            validate { it.demandValue("aktiv", true) }
            validate {
                it.requireKey(
                    "namespace",
                    "appnavn",
                    "eventId",
                    "forstBehandlet",
                    "fodselsnummer",
                    "tekst",
                    "link",
                    "sikkerhetsnivaa",
                    "eksternVarsling"
                )
            }
            validate {
                it.interestedIn(
                    "prefererteKanaler",
                    "smsVarslingstekst",
                    "epostVarslingstekst",
                    "epostVarslingstittel"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val innboks = Innboks(
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
            aktiv = packet["aktiv"].booleanValue(),
            eksternVarsling = packet["eksternVarsling"].booleanValue(),
            prefererteKanaler = packet["prefererteKanaler"].map { it.textValue() },
            smsVarslingstekst = packet["smsVarslingstekst"].textValue(),
            epostVarslingstekst = packet["epostVarslingstekst"].textValue(),
            epostVarslingstittel = packet["epostVarslingstittel"].textValue()
        )

        runBlocking {
            varselRepository.persistVarsel(innboks)
            varselAktivertProducer.varselAktivert(innboks)
            log.info("Behandlet innboks fra rapid med eventid ${innboks.eventId}")

            rapidMetricsProbe.countProcessed(EventType.INNBOKS_INTERN, innboks.appnavn)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }
}
