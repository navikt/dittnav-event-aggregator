package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.shouldBe
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.done.rest.DoneRapidProducer
import no.nav.personbruker.dittnav.eventaggregator.done.rest.doneApi
import no.nav.tms.token.support.authentication.installer.mock.installMockedAuthenticators
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DoneApiTest {
    private val doneEndpoint = "/done"
    private val database = LocalPostgresDatabase.migratedDb()
    private val beskjedRepository = BeskjedRepository(database)
    private val mockProducer = MockProducer(
        false,
        StringSerializer(),
        StringSerializer()
    )
    private val rapidProducer = DoneRapidProducer(mockProducer, "testtopic")
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
        eventId = "123465lhgh",
        fodselsnummer = apiTestfnr,
        synligFremTil = LocalDateTime.now().plusHours(1),
        aktiv = true,
        systembruker = systembruker,
        namespace = namespace,
        appnavn = appnavn
    )


    @BeforeEach
    fun populate() {
        runBlocking {
            database.dbQuery {
                createBeskjed(aktivBeskjed)
                createBeskjed(inaktivBeskjed)
            }
        }
    }

    @AfterEach
    fun cleanup() {
        runBlocking {
            database.dbQuery {
                deleteAllBeskjed()
            }
        }

        mockProducer.clear()
    }

    @Test
    fun `inaktiverer varsel og returnerer 200`() {
        testApplication {
            mockDoneApi()
            client.request {
                url(doneEndpoint)
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("""{"eventId": "${aktivBeskjed.eventId}"}""")
            }.status shouldBe HttpStatusCode.OK
        }

        mockProducer.history().size shouldBe 1
    }


    @Test
    fun `200 for allerede inaktiverte varsel`() {
        testApplication {
            mockDoneApi()
            client.request {
                url(doneEndpoint)
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("""{"eventId": "${inaktivBeskjed.eventId}"}""")
            }.status shouldBe HttpStatusCode.OK
        }

        mockProducer.history().size shouldBe 0
    }

    @Test
    fun `400 for varsel som ikke finnes`() {
        testApplication {
            mockDoneApi()
            val response = client.request {
                url(doneEndpoint)
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("""{"eventId": "7777777777"}""")
            }
            response.status shouldBe HttpStatusCode.BadRequest
            response.bodyAsText() shouldBe "beskjed med eventId 7777777777 ikke funnet"

            mockProducer.history().size shouldBe 0
        }
    }

    @Test
    fun `400 når eventId mangler`() {
        testApplication {
            mockDoneApi()
            val response = client.request {
                method = HttpMethod.Post
                url(doneEndpoint)
                header("Content-Type", "application/json")
                setBody("""{"event": "12398634581111"}""")
            }
            response.status shouldBe HttpStatusCode.BadRequest
            response.bodyAsText() shouldBe "eventid parameter mangler"
            mockProducer.history().size shouldBe 0
        }
    }

    @Test
    fun `401 for uantentisert bruker`() {
    }

    private fun ApplicationTestBuilder.mockDoneApi() {
        application {
            doneApi(
                beskjedRepository = beskjedRepository,
                producer = rapidProducer,
                installAuthenticatorsFunction = {
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
    }
}


