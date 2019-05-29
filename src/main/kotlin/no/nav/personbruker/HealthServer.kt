package no.nav.personbruker

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports


object HealthServer {

    suspend fun startServer(port: Int): NettyApplicationEngine {
        DefaultExports.initialize()
        return embeddedServer(Netty, port = port) {
            install(DefaultHeaders)
            routing {
                get("/metrics") {
                    val names = call.request.queryParameters.getAll("name")?.toSet() ?: emptySet()
                    call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), HttpStatusCode.OK) {
                        TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
                    }
                }
                get("/isAlive") {
                    call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
                }

                get("/isReady") {
                    if (Consumer.isRunning()) {
                        call.respondText(text = "READY", contentType = ContentType.Text.Plain)
                    } else {
                        call.respondText(text = "NOTREADY", contentType = ContentType.Text.Plain, status = HttpStatusCode.FailedDependency)
                    }
                }
            }
        }
    }
}