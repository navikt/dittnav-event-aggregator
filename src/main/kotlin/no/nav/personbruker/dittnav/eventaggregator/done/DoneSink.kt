package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.RapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType.Inaktivert
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class DoneSink(
    rapidsConnection: RapidsConnection,
    private val varselRepository: VarselRepository,
    private val varselInaktivertProducer: VarselInaktivertProducer,
    private val rapidMetricsProbe: RapidMetricsProbe
) :
    River.PacketListener {

    private val log: Logger = LoggerFactory.getLogger(DoneSink::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "done") }
            validate { it.requireKey(
                "eventId",
                "forstBehandlet",
                "fodselsnummer"
            ) }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val done = Done(
            systembruker = "N/A",
            namespace = "N/A",
            appnavn = "N/A",
            eventId = packet["eventId"].textValue(),
            eventTidspunkt = packet["forstBehandlet"].asLocalDateTime(), //Felt ikke i bruk
            forstBehandlet = packet["forstBehandlet"].asLocalDateTime(),
            fodselsnummer = packet["fodselsnummer"].textValue(),
            grupperingsId = "N/A",
            sistBehandlet = nowAtUtc()
        )

        runBlocking {
            val varsel = varselRepository.getVarsel(done.eventId)

            if (varsel == null) {
                // lagre i ventetabell hvis ikke varsel finnes
                varselRepository.persistWaitingDone(done)
            } else {
                when (varsel.type) {
                    VarselType.BESKJED -> varselRepository.inaktiverVarsel(done, VarselType.BESKJED)
                    VarselType.OPPGAVE -> varselRepository.inaktiverVarsel(done, VarselType.OPPGAVE)
                    VarselType.INNBOKS -> varselRepository.inaktiverInnboks(done)
                }
                varselInaktivertProducer.varselInaktivert(
                    VarselHendelse(
                        Inaktivert,
                        varsel.type,
                        eventId = varsel.eventId,
                        namespace = varsel.namespace,
                        appnavn = varsel.appnavn
                    )
                )
            }

            log.info("Behandlet done fra rapid med eventid ${done.eventId}")
            rapidMetricsProbe.countProcessed(EventType.DONE_INTERN, done.appnavn)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }
}
