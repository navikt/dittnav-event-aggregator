package no.nav.personbruker.dittnav.eventaggregator.oppgave

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.asOptionalLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.RapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselAktivertProducer
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class OppgaveSink(
    rapidsConnection: RapidsConnection,
    private val varselRepository: VarselRepository,
    private val varselAktivertProducer: VarselAktivertProducer,
    private val rapidMetricsProbe: RapidMetricsProbe
) :
    River.PacketListener {

    private val log: Logger = LoggerFactory.getLogger(OppgaveSink::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "oppgave") }
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
            validate { it.interestedIn(
                "synligFremTil",
                "prefererteKanaler",
                "smsVarslingstekst",
                "epostVarslingstekst",
                "epostVarslingstittel"
            ) }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val oppgave = Oppgave(
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
            prefererteKanaler = packet["prefererteKanaler"].map { it.textValue() },
            smsVarslingstekst = packet["smsVarslingstekst"].textValue(),
            epostVarslingstekst = packet["epostVarslingstekst"].textValue(),
            epostVarslingstittel = packet["epostVarslingstittel"].textValue()
        )

        runBlocking {
            varselRepository.persistOppgave(oppgave)
            varselAktivertProducer.varselAktivert(oppgave)
            log.info("Behandlet oppgave fra rapid med eventid ${oppgave.eventId}")

            rapidMetricsProbe.countProcessed(EventType.OPPGAVE_INTERN, oppgave.appnavn)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }
}
