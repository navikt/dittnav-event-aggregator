package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.prometheus.client.hotspot.DefaultExports
import no.nav.personbruker.dittnav.eventaggregator.common.api.healthApi

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    DefaultExports.initialize()
    install(DefaultHeaders)
    routing {
        healthApi(appContext)

        configureStartupHook(appContext)
        configureShutdownHook(appContext)
    }

}

private fun Application.configureStartupHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStarted) {
        Flyway.runFlywayMigrations(appContext.environment)
        if (isOtherEnvironmentThanProd()) {
            KafkaConsumerSetup.startAllKafkaPollers(appContext)
            appContext.cachedDoneEventConsumer.poll()
        }
    }
}

private fun isOtherEnvironmentThanProd() = System.getenv("NAIS_CLUSTER_NAME") != "prod-sbs"

private fun Application.configureShutdownHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStopPreparing) {
        if (isOtherEnvironmentThanProd()) {
            KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
            appContext.cachedDoneEventConsumer.cancel()
        }
        appContext.database.dataSource.close()
    }
}
