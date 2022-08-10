package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository

internal class BeskjedSink(rapidsConnection: RapidsConnection, beskjedRepository: BeskjedRepository): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "beskjed") }
            validate { it.requireKey("fodselsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {

    }
}