package no.nav.personbruker.dittnav.eventaggregator.varsel

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class DoneSink(
    rapidsConnection: RapidsConnection,
    private val varselRepository: VarselRepository,
    private val rapidMetricsProbe: RapidMetricsProbe,
    private val writeToDb: Boolean
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
            val varsel = varselRepository.getBrukernotifikasjoner(done.eventId)

            if (writeToDb) {
                if (varsel.isEmpty()) {
                    // lagre i ventetabell hvis ikke varsel finnes
                    varselRepository.persistWaitingDone(done)
                } else {
                    when (varsel.first().type) {
                        EventType.BESKJED_INTERN -> varselRepository.inaktiverBeskjed(done)
                        EventType.OPPGAVE_INTERN -> varselRepository.inaktiverOppgave(done)
                        EventType.INNBOKS_INTERN -> varselRepository.inaktiverInnboks(done)
                        EventType.DONE_INTERN -> log.error("Prøvde å inaktivere done-event med eventid ${done.eventId}")
                    }
                }

                log.info("Behandlet done fra rapid med eventid ${done.eventId}")
            } else {
                log.info("Dryrun: done fra rapid med eventid ${done.eventId}")
            }
            rapidMetricsProbe.countProcessed(EventType.DONE_INTERN, done.appnavn)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }
}
