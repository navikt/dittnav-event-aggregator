package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
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
        configureShutdownHook(appContext)

        configureStartupHook(appContext)
        configureShutdownHook(appContext)
    }

    Flyway.runFlywayMigrations(appContext.environment)
}

private fun Application.configureStartupHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStarted) {
        Flyway.runFlywayMigrations(appContext.environment)
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
