package no.nav.personbruker.dittnav.eventaggregator.config

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildRapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.done.DoneSink
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.eksternvarslingstatus.EksternVarslingStatusRepository
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.eksternvarslingstatus.EksternVarslingStatusSink
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.eksternvarslingstatus.EksternVarslingStatusUpdater

fun main() {
    val appContext = ApplicationContext()

    startRapid(appContext.environment, appContext.database, appContext)
}

private fun startRapid(environment: Environment, database: Database, appContext: ApplicationContext) {
    val rapidMetricsProbe = buildRapidMetricsProbe(environment)
    val varselRepository = VarselRepository(database)
    val eksternVarslingStatusRepository = EksternVarslingStatusRepository(database)
    val eksternVarslingStatusUpdater = EksternVarslingStatusUpdater(eksternVarslingStatusRepository, varselRepository)
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
        EksternVarslingStatusSink(
            rapidsConnection = this,
            eksternVarslingStatusUpdater = eksternVarslingStatusUpdater,
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
                    appContext.stopAllArchivers()
                }
            }
        })
    }.start()
}
