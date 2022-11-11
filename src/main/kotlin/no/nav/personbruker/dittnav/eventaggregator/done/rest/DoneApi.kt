package no.nav.personbruker.dittnav.eventaggregator.done.rest

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
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
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedDoesNotBelongToUserException
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.log
import no.nav.tms.token.support.authentication.installer.installAuthenticators
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory


fun Application.doneApi(
    beskjedRepository: BeskjedRepository,
    producer: VarselInaktivertProducer,
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(),
) {

    installAuthenticatorsFunction()

    install(ContentNegotiation) {
        jackson {
            configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is BeskjedDoesNotBelongToUserException -> {
                    call.respond(HttpStatusCode.Unauthorized)
                    log.warn("Forsøk på å inaktivere beskjed ${cause.eventId} med feil personnummer")
                }

                is IllegalArgumentException -> {
                    call.respondText(
                        status = HttpStatusCode.BadRequest,
                        text = cause.message ?: "Feil i parametre"
                    )

                    log.warn(cause.message, cause.stackTrace)
                }

                else -> call.respond(HttpStatusCode.InternalServerError)
            }

        }
    }


    routing {
        authenticate {
            route("beskjed/done") {
                post {

                    val eventId = call.eventId()
                    val fnr = TokenXUserFactory.createTokenXUser(call).ident
                    beskjedRepository.setBeskjedInactive(eventId, fnr).also {
                        when (it) {
                            0 -> log.info("Forsøk på inaktivere beskjed som allerede er innatkivert med eventid $eventId")
                            1 -> producer.cancelEksternVarsling(eventId)
                        }
                    }
                    call.respond(HttpStatusCode.OK)
                }
            }
        }

        authenticate(AzureAuthenticator.name) {
            route("/on-behalf-of/beskjed/done") {
                post {
                    val eventId = call.eventId()
                    val fnr = call.request.headers["fodselsnummer"] ?: throw FnrHeaderMissingException()
                    beskjedRepository.setBeskjedInactive(
                        eventId,
                        fnr
                    ).also {
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

private suspend fun ApplicationCall.eventId(): String =
    receive<EventIdBody>().eventId ?: throw EventIdMissingException()

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
class FnrHeaderMissingException : IllegalArgumentException("header fodselsnummer mangler")