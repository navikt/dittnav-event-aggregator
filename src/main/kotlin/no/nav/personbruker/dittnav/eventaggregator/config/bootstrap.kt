package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.polling.pollingApi
import no.nav.personbruker.dittnav.eventaggregator.done.waitTableApi
import no.nav.personbruker.dittnav.eventaggregator.health.healthApi

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    DefaultExports.initialize()
    install(DefaultHeaders)
    routing {
        healthApi(appContext.healthService)
        pollingApi(appContext)
        waitTableApi(appContext)
    }

    configureStartupHook(appContext)
    configureShutdownHook(appContext)
}

private fun Application.configureStartupHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStarted) {
        Flyway.runFlywayMigrations(appContext.environment)
        KafkaConsumerSetup.startAllKafkaPollers(appContext)
        appContext.periodicDoneEventWaitingTableProcessor.start()
        appContext.periodicConsumerPollingCheck.start()
    }
}

private fun Application.configureShutdownHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStopPreparing) {
        runBlocking {
            KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
            appContext.periodicDoneEventWaitingTableProcessor.stop()
            appContext.periodicConsumerPollingCheck.stop()
        }
        appContext.database.dataSource.close()
    }
}
