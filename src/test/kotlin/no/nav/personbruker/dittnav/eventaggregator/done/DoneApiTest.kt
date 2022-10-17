package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.shouldBe
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.done.rest.DoneRapidProducer
import no.nav.personbruker.dittnav.eventaggregator.done.rest.doneApi
import no.nav.tms.token.support.authentication.installer.mock.installMockedAuthenticators
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DoneApiTest {
    private val doneEndpoint = "/done"
    private val doneRepository = DoneRepository(LocalPostgresDatabase.migratedDb())
    private val rapidProducer = DoneRapidProducer()
    private val apiTestfnr = "134567890"
    private val systembruker = "dummyTestBruker"
    private val namespace = "min-side"
    private val appnavn = "dittnav"

    private val inaktivBeskjed = BeskjedTestData.beskjed(
        eventId = "123465abnhkfg",
        fodselsnummer = apiTestfnr,
        aktiv = false,
        systembruker = systembruker,
        namespace = namespace,
        appnavn = appnavn
    )

    private val aktivBeskjed = BeskjedTestData.beskjed(
        eventId = "123465abnhkfg",
        fodselsnummer = apiTestfnr,
        synligFremTil = LocalDateTime.now().plusHours(1),
        aktiv = true,
        systembruker = systembruker,
        namespace = namespace,
        appnavn = appnavn
    )


    @BeforeAll
    fun populate() {
    }

    @AfterEach()
    fun clean() {

    }

    @Test
    fun `inaktiverer varsel og returnerer 200`() =
        testApplication {
            application {
                doneApi(repository = doneRepository, producer = rapidProducer, installAuthenticatorsFunction = {
                    installMockedAuthenticators {
                        installTokenXAuthMock {
                            setAsDefault = true
                            alwaysAuthenticated = true
                            staticUserPid = apiTestfnr
                            staticSecurityLevel = SecurityLevel.LEVEL_4
                        }
                        installAzureAuthMock {}
                    }
                })
            }
            val response = client.request {
                url(doneEndpoint)
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("""{"eventId": "${aktivBeskjed.eventId}"}""")
            }
            response.status shouldBe HttpStatusCode.OK
        }
    /*

    @Test
    fun `200 for allerede inaktiverte varsel`() {
        testApplication {
            mockEventHandlerApi(
                doneEventService = doneEventService
            )
            val response = client.request{
                method = HttpMethod.Post
                url(doneEndpoint)
                header("Content-Type", "application/json")
                setBody("""{"eventId": "${inaktivBeskjed.eventId}"}""")
            }
            response.status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `400 for varsel som ikke finnes`() {
        testApplication {
            mockEventHandlerApi(
                doneEventService = doneEventService
            )
            val response = client.request {
                method = HttpMethod.Post
                url(doneEndpoint)
                header("Content-Type", "application/json")
                setBody("""{"eventId": "12311111111"}""")
            }
            response.status shouldBe HttpStatusCode.BadRequest
            response.bodyAsText() shouldBe "beskjed med eventId 12311111111 ikke funnet"

        }
    }

    @Test
    fun `400 når eventId mangler`() {
        testApplication{
            mockEventHandlerApi(
                doneEventService = doneEventService
            )
            val result = client.request {
                method = HttpMethod.Post
                url(doneEndpoint)
                header("Content-Type", "application/json")
                setBody("""{"event": "12398634581111"}""")
            }
            result.status shouldBe HttpStatusCode.BadRequest
            result.bodyAsText() shouldBe "eventid parameter mangler"
        }
    }
    */

}