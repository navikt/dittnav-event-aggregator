package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {

    val appContext = ApplicationContext()

    embeddedServer(Netty, port = 8080) {
        eventAggregatorApi(
            appContext
        )
    }.start(wait = true)

}