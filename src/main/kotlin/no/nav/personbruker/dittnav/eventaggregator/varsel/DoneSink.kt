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
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class DoneSink(
    rapidsConnection: RapidsConnection,
    private val doneRepository: DoneRepository
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
            val varsel = doneRepository.fetchBrukernotifikasjonerFromViewForEventIds(listOf(done.eventId))
            if(varsel.isEmpty()) {
                // lagre i ventetabell hvis ikke varsel finnes
                doneRepository.createInOneBatch(listOf(done))
            }
            else {
                when(varsel.first().type) {
                    EventType.BESKJED_INTERN -> doneRepository.writeDoneEventsForBeskjedToCache(listOf(done))
                    EventType.OPPGAVE_INTERN -> doneRepository.writeDoneEventsForOppgaveToCache(listOf(done))
                    EventType.INNBOKS_INTERN -> doneRepository.writeDoneEventsForInnboksToCache(listOf(done))
                    EventType.DONE_INTERN -> log.error("Prøvde å inaktivere done-event med eventid ${done.eventId}")
                }
            }

            log.info("Behandlet done fra rapid med eventid ${done.eventId}")
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }
}
