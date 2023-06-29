package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType

internal class BeskjedInaktivertAvBrukerSink(
    rapidsConnection: RapidsConnection,
    private val varselRepository: VarselRepository,
    private val varselInaktivertProducer: VarselInaktivertProducer
) :
    River.PacketListener {

    private val log = KotlinLogging.logger { }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "inaktivert") }
            validate { it.rejectValue("@source", "aggregator") }
            validate { it.demandValue("varselType", "beskjed") }
            validate { it.demandValue("kilde", "bruker") }
            validate { it.requireKey("varselId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["varselId"].textValue()

        runBlocking {
            val varsel = varselRepository.getVarsel(eventId)

            if (varsel == null || varsel.type != VarselType.BESKJED) {
                log.warn("Fant ikke beskjed med id")
            } else if (!varsel.aktiv) {
                log.info("Hopper over allerede inaktiv beskjed med id [${varsel.eventId}].")
            } else {
                log.info("Speiler bruker-initiert inaktivering av beskjed hos authority med varselId $eventId")

                varselRepository.inaktiverVarsel(eventId, VarselType.BESKJED)

                varselInaktivertProducer.varselInaktivert(
                    VarselHendelse(
                        HendelseType.Inaktivert,
                        varsel.type,
                        eventId = varsel.eventId,
                        namespace = varsel.namespace,
                        appnavn = varsel.appnavn
                    ),
                    kilde = VarselInaktivertKilde.Bruker
                )
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }
}
