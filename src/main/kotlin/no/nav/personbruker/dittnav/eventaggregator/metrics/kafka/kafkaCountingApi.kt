package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd

fun Routing.kafkaCountingApi(kafkaEventCounterService: KafkaEventCounterService, kafkaTopicEventCounterService: KafkaTopicEventCounterService) {

    get("/internal/kafka/count/all") {
        val numberOfEvents = kafkaEventCounterService.countAllEvents()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/kafka/count/unique") {
        val numberOfEvents = kafkaEventCounterService.countUniqueEvents()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/kafka/count/beskjed") {
        var kafkaTopicParameter: String? = call.request.queryParameters["topic"]
        val responseText = countEventsOfType(kafkaTopicParameter, EventType.BESKJED, kafkaEventCounterService, kafkaTopicEventCounterService)
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    if (isOtherEnvironmentThanProd()) {
        get("/internal/kafka/count/innboks") {
            var kafkaTopicParameter: String? = call.request.queryParameters["topic"]
            val responseText = countEventsOfType(kafkaTopicParameter, EventType.INNBOKS, kafkaEventCounterService, kafkaTopicEventCounterService)
            call.respondText(text = responseText, contentType = ContentType.Text.Plain)
        }
    }

    get("/internal/kafka/count/oppgave") {
        var kafkaTopicParameter: String? = call.request.queryParameters["topic"]
        val responseText = countEventsOfType(kafkaTopicParameter, EventType.OPPGAVE, kafkaEventCounterService, kafkaTopicEventCounterService)
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/kafka/count/done") {
        var kafkaTopicParameter: String? = call.request.queryParameters["topic"]
        val responseText = countEventsOfType(kafkaTopicParameter, EventType.DONE, kafkaEventCounterService, kafkaTopicEventCounterService)
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }
}

private fun countEventsOfType(kafkaTopicParameter: String?,
                              eventType: EventType,
                              kafkaEventCounterService: KafkaEventCounterService,
                              kafkaTopicEventCounterService: KafkaTopicEventCounterService): String {
    var topicName: String
    var numberOfEvents: Long
    if (kafkaTopicParameter.isNullOrBlank()) {
        topicName = getDefaultTopicName(eventType)
        numberOfEvents = when (eventType) {
            EventType.OPPGAVE -> kafkaEventCounterService.countOppgaver()
            EventType.BESKJED -> kafkaEventCounterService.countBeskjeder()
            EventType.INNBOKS -> kafkaEventCounterService.countInnboksEventer()
            EventType.DONE -> kafkaEventCounterService.countDoneEvents()
        }
    } else {
        topicName = kafkaTopicParameter
        numberOfEvents = kafkaTopicEventCounterService.countEventsOnTopic(topicName, eventType)
    }
    return "Antall eventer på topic $topicName: $numberOfEvents"
}


