package no.nav.personbruker.dittnav.eventaggregator.done.rest

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.tms.token.support.authentication.installer.installAuthenticators


fun Application.doneApi(
    repository: DoneRepository,
    producer: DoneRapidProducer,
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(),
) {

    installAuthenticatorsFunction()

    install(ContentNegotiation) {
        jackson() {
            configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    routing {
        authenticate {
            route("/done") {
                post {
                    call.respond(HttpStatusCode.NotImplemented)
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