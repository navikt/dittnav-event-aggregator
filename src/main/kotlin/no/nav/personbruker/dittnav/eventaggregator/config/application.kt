package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildRapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.DoneSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository

fun main() {
    val appContext = ApplicationContext()

    if(appContext.environment.rapidOnly) {
        startRapid(appContext.environment, appContext.doneRepository, appContext.database)
    }
    else {
        embeddedServer(Netty, port = 8080) {
            eventAggregatorApi(
                appContext
            )
        }.start(wait = true)
    }
}

private fun startRapid(environment: Environment, doneRepository: DoneRepository, database: Database) {
    val rapidMetricsProbe = buildRapidMetricsProbe(environment)
    val varselRepository = VarselRepository(database)
    RapidApplication.create(environment.rapidConfig() + mapOf("HTTP_PORT" to "8080")).apply {
        BeskjedSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = environment.rapidWriteToDb
        )
        OppgaveSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = environment.rapidWriteToDb
        )
        InnboksSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = environment.rapidWriteToDb
        )
        DoneSink(
            rapidsConnection = this,
            doneRepository = doneRepository,
            rapidMetricsProbe = rapidMetricsProbe,
            writeToDb = environment.rapidWriteToDb
        )
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(environment)
            }
        })
    }.start()
}
