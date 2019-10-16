package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.application.*
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import no.nav.personbruker.dittnav.eventaggregator.common.api.healthApi
import no.nav.personbruker.dittnav.eventaggregator.done.doneEventsApi
import no.nav.personbruker.dittnav.eventaggregator.informasjon.informasjonEventsApi
import no.nav.personbruker.dittnav.eventaggregator.melding.meldingEventsApi
import no.nav.personbruker.dittnav.eventaggregator.oppgave.oppgaveEventsApi

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    DefaultExports.initialize()
    install(DefaultHeaders)
    routing {
        healthApi(appContext)
        doneEventsApi(appContext.doneProducer)
        informasjonEventsApi(appContext.informasjonProducer)
        oppgaveEventsApi(appContext.oppgaveProducer)
        meldingEventsApi(appContext.meldingProducer)
        get("/metrics") {
            val names = call.request.queryParameters.getAll("name")?.toSet() ?: emptySet()
            call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), HttpStatusCode.OK) {
                TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
            }
        }

        configureStartupHook(appContext)
        configureShutdownHook(appContext)
    }

    Flyway.runFlywayMigrations(appContext.environment)
}

private fun Application.configureStartupHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStarted) {
        KafkaConsumerSetup.startAllKafkaPollers(appContext)
        appContext.cachedDoneEventConsumer.poll()
    }
}

private fun Application.configureShutdownHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStopPreparing) {
        KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
        appContext.cachedDoneEventConsumer.cancel()
    }
}
