package no.nav.personbruker.dittnav.eventaggregator.config

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.brukernotifikasjon.schemas.Melding
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.common.api.healthApi
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.done.CachedDoneEventConsumer
import no.nav.personbruker.dittnav.eventaggregator.done.DoneProducer
import no.nav.personbruker.dittnav.eventaggregator.done.doneEventsApi
import no.nav.personbruker.dittnav.eventaggregator.informasjon.InformasjonProducer
import no.nav.personbruker.dittnav.eventaggregator.informasjon.informasjonEventsApi
import no.nav.personbruker.dittnav.eventaggregator.melding.MeldingProducer
import no.nav.personbruker.dittnav.eventaggregator.melding.meldingEventsApi
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveProducer
import no.nav.personbruker.dittnav.eventaggregator.oppgave.oppgaveEventsApi
import java.util.concurrent.TimeUnit

object Server {

    const val portNumber = 8080

    var environment = Environment()
    lateinit var database: Database

    lateinit var infoConsumer: Consumer<Informasjon>
    lateinit var meldingConsumer: Consumer<Melding>
    lateinit var oppgaveConsumer: Consumer<Oppgave>
    lateinit var doneConsumer: Consumer<Done>
    lateinit var cachedDoneEventConsumer: CachedDoneEventConsumer

    fun configure(): NettyApplicationEngine {
        DefaultExports.initialize()
        val app = embeddedServer(Netty, port = portNumber) {
            install(DefaultHeaders)
            routing {
                healthApi()
                doneEventsApi(DoneProducer)
                informasjonEventsApi(InformasjonProducer)
                oppgaveEventsApi(OppgaveProducer)
                meldingEventsApi(MeldingProducer)
                get("/metrics") {
                    val names = call.request.queryParameters.getAll("name")?.toSet() ?: emptySet()
                    call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), HttpStatusCode.OK) {
                        TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
                    }
                }
            }
        }

        database = PostgresDatabase(environment)
        Flyway.runFlywayMigrations(environment)

        KafkaConsumerSetup.initializeTheKafkaConsumers(environment)
        cachedDoneEventConsumer = CachedDoneEventConsumer(database = database)
        cachedDoneEventConsumer.poll()

        addGraceTimeAtShutdownToAllowRunningRequestsToComplete(app)
        return app
    }

    private fun addGraceTimeAtShutdownToAllowRunningRequestsToComplete(app: NettyApplicationEngine) {
        Runtime.getRuntime().addShutdownHook(Thread {
            KafkaConsumerSetup.stopAllKafkaConsumers()
            cachedDoneEventConsumer.cancel()
            app.stop(5, 60, TimeUnit.SECONDS)
        })
    }

}
