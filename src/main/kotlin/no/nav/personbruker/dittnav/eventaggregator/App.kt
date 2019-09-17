package no.nav.personbruker.dittnav.eventaggregator

import no.nav.personbruker.dittnav.eventaggregator.config.Server

fun main() {
    val server = Server.configure()
    server.start()
}
