package no.nav.personbruker.dittnav.eventaggregator.done

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getBeskjedByEventId
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
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
    private val doneEndpoint = "/beskjed/done"
    private val database = LocalPostgresDatabase.migratedDb()
    private val beskjedRepository = BeskjedRepository(database)
    private val mockProducer = MockProducer(
        false,
        StringSerializer(),
        StringSerializer()
    )
    private val rapidProducer = VarselInaktivertProducer(mockProducer, "testtopic", mockk(relaxed = true))
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
            client.doneRequest(
                body = """{"eventId": "${aktivBeskjed.eventId}"}"""
            ).status shouldBe HttpStatusCode.OK
        }

        runBlocking {
            database.dbQuery {
                getBeskjedByEventId(aktivBeskjed.eventId)
            }.fristUtløpt shouldBe false
        }

        mockProducer.history().size shouldBe 1
        jacksonObjectMapper().readTree(mockProducer.history().first().value()).apply {
            this["eventId"].asText() shouldBe aktivBeskjed.eventId
            this["@event_name"].asText() shouldBe "inaktivert"
        }
    }


    @Test
    fun `200 for allerede inaktiverte varsel`() {
        testApplication {
            mockDoneApi()
            client.doneRequest(
                body = """{"eventId": "${inaktivBeskjed.eventId}"}"""
            ).status shouldBe HttpStatusCode.OK

            mockProducer.history().size shouldBe 0
        }
    }

    @Test
    fun `400 for varsel som ikke finnes`() {
        testApplication {
            mockDoneApi()
            val response = client.doneRequest(body = """{"eventId": "7777777777"}""")

            response.status shouldBe HttpStatusCode.BadRequest
            mockProducer.history().size shouldBe 0
        }
    }


    @Test
    fun `400 når eventId mangler`() {
        testApplication {
            mockDoneApi()
            val response = client.doneRequest(body = """{"event": "12398634581111"}""")
            response.status shouldBe HttpStatusCode.BadRequest
            mockProducer.history().size shouldBe 0
        }
    }

    @Test
    fun `401 for uantentisert bruker`() = testApplication {
        mockDoneApi(authenticated = false)
        client.doneRequest(
            body = """{"eventId": "${aktivBeskjed.eventId}"}"""
        ).status shouldBe HttpStatusCode.Unauthorized
        mockProducer.history().size shouldBe 0

    }

    @Test
    fun `401 for forsøk på deaktivering av eventid som ikke matcher fødeslsnummer`() = testApplication {
        mockDoneApi(tokenForfnr = "45670987987")
        client.doneRequest(
            body = """{"eventId": "${aktivBeskjed.eventId}"}"""
        ).status shouldBe HttpStatusCode.Unauthorized

        mockProducer.history().size shouldBe 0
    }

    private fun ApplicationTestBuilder.mockDoneApi(authenticated: Boolean = true, tokenForfnr: String = apiTestfnr) {
        application {
            doneApi(
                beskjedRepository = beskjedRepository,
                producer = rapidProducer,
                installAuthenticatorsFunction = {
                    installMockedAuthenticators {
                        installTokenXAuthMock {
                            setAsDefault = true
                            alwaysAuthenticated = authenticated
                            staticUserPid = tokenForfnr
                            staticSecurityLevel = SecurityLevel.LEVEL_4
                        }
                        installAzureAuthMock {}
                    }
                })
        }
    }

    private suspend fun HttpClient.doneRequest(body: String) = request {
        url(doneEndpoint)
        method = HttpMethod.Post
        header("Content-Type", "application/json")
        setBody(body)
    }
}


