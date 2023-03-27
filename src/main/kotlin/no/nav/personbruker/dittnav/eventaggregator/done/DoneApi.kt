package no.nav.personbruker.dittnav.eventaggregator.done

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedDoesNotBelongToUserException
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertKilde.Bruker
import no.nav.tms.token.support.authentication.installer.installAuthenticators
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory



internal val log = KotlinLogging.logger {  }
internal val sikkerlog = KotlinLogging.logger ("secureLog" )

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
                    log.warn("Forsøk på å inaktivere beskjed ${cause.eventId} med feil personnummer")
                    call.respond(HttpStatusCode.Unauthorized)

                }

                is IllegalArgumentException -> {
                    log.warn ("Illegal argument exception i kall til done-api ")
                    sikkerlog.warn(cause.message, cause.stackTrace)
                    call.respond(HttpStatusCode.BadRequest)

                }

                else -> {
                    log.error { "Feil i kall til done-api: ${cause.message}" }
                    sikkerlog.error("""Feil i kall til done-api: ${cause.message}
                       stacktrace: 
                       ${cause.stackTrace} 
                    """)
                    call.respond(HttpStatusCode.InternalServerError)
                }
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
                            null -> log.info("Forsøk på inaktivere beskjed som allerede er inaktivert med eventid $eventId")
                            else -> producer.varselInaktivert(it, Bruker)
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
                            null -> log.warn("Forsøk på inaktivere beskjed som allerede er inaktivert med eventid $eventId")
                            else -> producer.varselInaktivert(it, Bruker)
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
