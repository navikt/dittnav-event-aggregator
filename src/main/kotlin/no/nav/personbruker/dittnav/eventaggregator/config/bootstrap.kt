package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.polling.pollingApi
import no.nav.personbruker.dittnav.eventaggregator.done.waitTableApi
import no.nav.personbruker.dittnav.eventaggregator.health.healthApi
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildRapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.DoneSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.OppgaveSink
import kotlin.concurrent.thread

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

        if (appContext.environment.rapidEnabled) {
            thread {
                startRapid(appContext)
            }
        }
        KafkaConsumerSetup.startAllKafkaPollers(appContext)
        appContext.periodicDoneEventWaitingTableProcessor.start()
        appContext.periodicConsumerPollingCheck.start()
        appContext.periodicExpiredBeskjedProcessor.start()
        appContext.startAllArchivers()
    }
}

private fun startRapid(appContext: ApplicationContext) {
    val rapidMetricsProbe = buildRapidMetricsProbe(appContext.environment)
    RapidApplication.create(appContext.environment.rapidConfig()).apply {
        BeskjedSink(
            rapidsConnection = this,
            beskjedRepository = appContext.beskjedRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = appContext.environment.rapidWriteToDb
        )
        OppgaveSink(this, appContext.oppgaveRepository, appContext.environment.rapidWriteToDb)
        InnboksSink(this, appContext.innboksRepository, appContext.environment.rapidWriteToDb)
        DoneSink(this, appContext.doneRepository, appContext.environment.rapidWriteToDb)
    }.start()
}

private fun Application.configureShutdownHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStopPreparing) {
        runBlocking {
            KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
            appContext.periodicDoneEventWaitingTableProcessor.stop()
            appContext.periodicConsumerPollingCheck.stop()
            appContext.periodicExpiredBeskjedProcessor.stop()
            appContext.kafkaProducerDone.flushAndClose()
            appContext.stopAllArchivers()
        }
        appContext.database.dataSource.close()
    }
}
