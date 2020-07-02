package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.done.waitTableApi
import no.nav.personbruker.dittnav.eventaggregator.health.healthApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.count.cacheCountingApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.eventCountingApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.kafkaCountingApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.pollingApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.submitter.metricsSubmitterApi

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    DefaultExports.initialize()
    install(DefaultHeaders)
    routing {
        healthApi(appContext.healthService)
        kafkaCountingApi(appContext.kafkaEventCounterService, appContext.kafkaTopicEventCounterService)
        cacheCountingApi(appContext.cacheEventCounterService)
        eventCountingApi(appContext.kafkaEventCounterService, appContext.cacheEventCounterService)
        pollingApi(appContext)
        waitTableApi(appContext)
        metricsSubmitterApi(appContext)
    }

    configureStartupHook(appContext)
    configureShutdownHook(appContext)
}

private fun Application.configureStartupHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStarted) {
        Flyway.runFlywayMigrations(appContext.environment)
        KafkaConsumerSetup.startAllKafkaPollers(appContext)
        appContext.periodicDoneEventWaitingTableProcessor.start()
    }
}

private fun Application.configureShutdownHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStopPreparing) {
        runBlocking {
            KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
            appContext.periodicDoneEventWaitingTableProcessor.stop()
            appContext.periodicMetricsSubmitter.stop()
        }
        appContext.database.dataSource.close()
        appContext.closeAllKafkaCountConsumers()
    }
}
