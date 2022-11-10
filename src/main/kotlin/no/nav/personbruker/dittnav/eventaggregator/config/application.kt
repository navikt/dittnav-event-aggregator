package no.nav.personbruker.dittnav.eventaggregator.config

import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.RapidApplicationConfig.Companion.fromEnv
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternVarslingStatusRepository
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternVarslingStatusSink
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternVarslingStatusUpdater
import no.nav.personbruker.dittnav.eventaggregator.done.DoneSink
import no.nav.personbruker.dittnav.eventaggregator.done.rest.VarselInaktivertRapidProducer
import no.nav.personbruker.dittnav.eventaggregator.done.rest.doneApi
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildRapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

fun main() {
    val appContext = ApplicationContext()

    startRapid(appContext.environment, appContext.database, appContext)
}

private fun startRapid(environment: Environment, database: Database, appContext: ApplicationContext) {
    val rapidMetricsProbe = buildRapidMetricsProbe(environment)
    val varselRepository = VarselRepository(database)
    val eksternVarslingStatusRepository = EksternVarslingStatusRepository(database)
    val eksternVarslingStatusUpdater = EksternVarslingStatusUpdater(eksternVarslingStatusRepository, varselRepository)
    RapidApplication.Builder(fromEnv(environment.rapidConfig())).withKtorModule {
        doneApi(
            beskjedRepository = BeskjedRepository(database = database),
            producer = VarselInaktivertRapidProducer(
                kafkaProducer = initializeRapidKafkaProducer(environment),
                topicName = environment.rapidTopic,
                rapidMetricsProbe = rapidMetricsProbe
            )
        )
    }.build().apply {
        BeskjedSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe
        )
        OppgaveSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe
        )
        InnboksSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe
        )
        DoneSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            rapidMetricsProbe = rapidMetricsProbe
        )
        EksternVarslingStatusSink(
            rapidsConnection = this,
            eksternVarslingStatusUpdater = eksternVarslingStatusUpdater
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


private fun initializeRapidKafkaProducer(environment: Environment) = KafkaProducer<String, String>(
    Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.aivenBrokers)
        put(
            ProducerConfig.CLIENT_ID_CONFIG,
            "dittnav-event-aggregator"
        )
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 40000)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
        environment.securityConfig.variables.also { securityVars ->
            put(
                KafkaAvroSerializerConfig.USER_INFO_CONFIG,
                "${securityVars.aivenSchemaRegistryUser}:${securityVars.aivenSchemaRegistryPassword}"
            )
            put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO")
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, securityVars.aivenTruststorePath)
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, securityVars.aivenCredstorePassword)
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, securityVars.aivenKeystorePath)
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, securityVars.aivenCredstorePassword)
            put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, securityVars.aivenCredstorePassword)
            put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
        }
    }
)
