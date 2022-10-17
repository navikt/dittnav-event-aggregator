package no.nav.personbruker.dittnav.eventaggregator.done.rest

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedNotFoundException
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.log
import no.nav.tms.token.support.authentication.installer.installAuthenticators


fun Application.doneApi(
    beskjedRepository: BeskjedRepository,
    producer: DoneRapidProducer,
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(),
) {

    installAuthenticatorsFunction()

    install(ContentNegotiation) {
        jackson() {
            configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> call.respondText(
                    status = HttpStatusCode.BadRequest,
                    text = cause.message ?: "Feil i parametre"
                )

                else -> call.respond(HttpStatusCode.InternalServerError)
            }

        }
    }


    routing {
        authenticate {
            route("/done") {
                post {

                    val eventId = call.receive<EventIdBody>().eventId ?: throw EventIdMissingException()
                    beskjedRepository.setBeskjedInactive(eventId).also {
                        when (it) {
                            0 -> log.warn("Forsøk på inaktivere beskjed som allerede er innatkivert med eventid $eventId")
                            1 -> producer.cancelEksternVarsling(eventId)
                        }
                    }
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }


}

private fun installAuth(): Application.() -> Unit = {
    installAuthenticators {
        installTokenXAuth {
            setAsDefault = true
        }
        installAzureAuth {
            setAsDefault = false
        }
    }
}

data class EventIdBody(val eventId: String? = null)

class EventIdMissingException : IllegalArgumentException("eventid parameter mangler")