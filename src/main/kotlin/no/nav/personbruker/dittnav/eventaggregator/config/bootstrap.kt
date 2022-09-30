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
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus.EksternVarslingStatusRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus.EksternVarslingStatusSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus.EksternVarslingStatusUpdater
import kotlin.concurrent.thread

fun Application.eventAggregatorApi(appContext: ApplicationContext) {
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
        appContext.periodicExpiredVarselProcessor.start()
        appContext.startAllArchivers()
    }
}

private fun startRapid(appContext: ApplicationContext) {
    val rapidMetricsProbe = buildRapidMetricsProbe(appContext.environment)
    val varselRepository = VarselRepository(appContext.database)
    val eksternVarslingStatusRepository = EksternVarslingStatusRepository(appContext.database)
    val eksternVarslingStatusUpdater = EksternVarslingStatusUpdater(eksternVarslingStatusRepository, varselRepository)
    RapidApplication.create(appContext.environment.rapidConfig() + mapOf("HTTP_PORT" to "8090")).apply {
        BeskjedSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = appContext.environment.rapidWriteToDb
        )
        OppgaveSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = appContext.environment.rapidWriteToDb
        )
        InnboksSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = appContext.environment.rapidWriteToDb
        )
        DoneSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = appContext.environment.rapidWriteToDb
        )
        EksternVarslingStatusSink(
            rapidsConnection = this,
            eksternVarslingStatusUpdater = eksternVarslingStatusUpdater,
            writeToDb = appContext.environment.rapidWriteToDb
        )
    }.start()
}

private fun Application.configureShutdownHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStopPreparing) {
        runBlocking {
            KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
            appContext.periodicDoneEventWaitingTableProcessor.stop()
            appContext.periodicConsumerPollingCheck.stop()
            appContext.periodicExpiredVarselProcessor.stop()
            appContext.kafkaProducerDone.flushAndClose()
            appContext.stopAllArchivers()
        }
        appContext.database.dataSource.close()
    }
}
