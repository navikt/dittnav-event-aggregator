package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.health.healthApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.cacheCountingApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.eventCountingApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.kafkaCountingApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.pollingApi

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    DefaultExports.initialize()
    install(DefaultHeaders)
    routing {
        healthApi(appContext.healthService)
        kafkaCountingApi(appContext.kafkaEventCounterService)
        cacheCountingApi(appContext.cacheEventCounterService)
        eventCountingApi(appContext.kafkaEventCounterService, appContext.cacheEventCounterService)
        pollingApi(appContext)

    }

    configureStartupHook(appContext)
    configureShutdownHook(appContext)
}

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
        runBlocking {
            KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
            appContext.cachedDoneEventConsumer.stopPolling()
        }
        appContext.database.dataSource.close()
        appContext.kafkaEventCounterService.closeAllConsumers()
    }
}
