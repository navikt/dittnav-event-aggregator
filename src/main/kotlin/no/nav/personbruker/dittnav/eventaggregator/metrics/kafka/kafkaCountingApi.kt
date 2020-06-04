package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.cio.Request
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd

fun Routing.kafkaCountingApi(kafkaEventCounterService: KafkaEventCounterService) {

    get("/internal/kafka/count/all") {
        val numberOfEvents = kafkaEventCounterService.countAllEvents()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/kafka/count/beskjed") {
        val kafkaTopicToCount: String? = call.request.queryParameters["topic"]
        val numberOfEvents = kafkaEventCounterService.countBeskjeder(kafkaTopicToCount)
        val responseText = "Antall beskjeder: $numberOfEvents"
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    if (isOtherEnvironmentThanProd()) {
        get("/internal/kafka/count/innboks") {
            val kafkaTopicToCount: String? = call.request.queryParameters["topic"]
            val numberOfEvents = kafkaEventCounterService.countInnboksEventer(kafkaTopicToCount)
            val responseText = "Antall innboks-eventer: $numberOfEvents"
            call.respondText(text = responseText, contentType = ContentType.Text.Plain)
        }
    }

    get("/internal/kafka/count/oppgave") {
        val kafkaTopicToCount: String? = call.request.queryParameters["topic"]
        val numberOfEvents = kafkaEventCounterService.countOppgaver(kafkaTopicToCount)
        val responseText = "Antall oppgaver: $numberOfEvents"
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/kafka/count/done") {
        val kafkaTopicToCount: String? = call.request.queryParameters["topic"]
        val numberOfEvents = kafkaEventCounterService.countDoneEvents(kafkaTopicToCount)
        val responseText = "Antall done-eventer: $numberOfEvents"
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

}
