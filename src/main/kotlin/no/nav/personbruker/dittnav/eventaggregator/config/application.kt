package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildRapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.DoneSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository

fun main() {
    val appContext = ApplicationContext()

    if(appContext.environment.rapidOnly) {
        startRapid(appContext.environment, appContext.database, appContext)
    }
    else {
        embeddedServer(Netty, port = 8080) {
            eventAggregatorApi(
                appContext
            )
        }.start(wait = true)
    }
}

private fun startRapid(environment: Environment, database: Database, appContext: ApplicationContext) {
    val rapidMetricsProbe = buildRapidMetricsProbe(environment)
    val varselRepository = VarselRepository(database)
    RapidApplication.create(environment.rapidConfig() + mapOf("HTTP_PORT" to "8080")).apply {
        BeskjedSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = true
        )
        OppgaveSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = true
        )
        InnboksSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = true
        )
        DoneSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = true
        )
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(environment)
                appContext.periodicDoneEventWaitingTableProcessor.start()
                appContext.startAllArchivers()
            }

            override fun onShutdown(rapidsConnection: RapidsConnection) {
                runBlocking {
                    appContext.periodicDoneEventWaitingTableProcessor.stop()
                    appContext.kafkaProducerDone.flushAndClose()
                    appContext.stopAllArchivers()
                }
            }
        })
    }.start()
}
