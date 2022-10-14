package no.nav.personbruker.dittnav.eventaggregator.done

import org.junit.jupiter.api.AfterEach

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DoneApiTest {
    private val doneEndpoint = "/done"
   // private val database = LocalPostgresDatabase.cleanDb()
   // private val doneEventService = DoneEventService(database = database)
    private val systembruker = "x-dittnav"
    private val namespace = "localhost"
    private val appnavn = "dittnav"
  /*  private val inaktivBeskjed = BeskjedObjectMother.createBeskjed(
        id = 1,
        eventId = "12387696478230",
        fodselsnummer = apiTestfnr,
        synligFremTil = OsloDateTime.now().plusHours(1),
        aktiv = false,
        systembruker = systembruker,
        namespace = namespace,
        appnavn = appnavn
    )
    private val aktivBeskjed = BeskjedObjectMother.createBeskjed(
        id = 2,
        eventId = "123465abnhkfg",
        fodselsnummer = apiTestfnr,
        synligFremTil = OsloDateTime.now().plusHours(1),
        aktiv = true,
        systembruker = systembruker,
        namespace = namespace,
        appnavn = appnavn
    )*/


    @BeforeAll
    fun populate() {
    }

    @AfterEach()
    fun clean() {

    }

    @Test
    fun `inaktiverer varsel og returnerer 200`() {}
/*
        testApplication {
            mockDoneApi(
                doneEventService = doneEventService,
                database = database
            )
            val response = client.request {
                url(doneEndpoint)
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("""{"eventId": "${aktivBeskjed.eventId}"}""")
            }
            response.status shouldBe HttpStatusCode.OK

    }

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
