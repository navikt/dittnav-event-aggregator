package no.nav.personbruker.dittnav.eventaggregator.config

import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.RapidApplicationConfig.Companion.fromEnv
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternVarslingOppdatertProducer
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternVarslingStatusRepository
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternVarslingStatusSink
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternVarslingStatusUpdater
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.DoneSink
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertProducer
import no.nav.personbruker.dittnav.eventaggregator.done.doneApi
import no.nav.personbruker.dittnav.eventaggregator.done.jobs.PeriodicDoneEventWaitingTableProcessor
import no.nav.personbruker.dittnav.eventaggregator.expired.ExpiredVarselRepository
import no.nav.personbruker.dittnav.eventaggregator.expired.PeriodicExpiredVarselProcessor
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildDBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildExpiredMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildRapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselAktivertProducer
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

fun main() {
    val appContext = ApplicationContext()

    startRapid(appContext.environment, appContext.database, appContext)
}

private fun startRapid(environment: Environment, database: Database, appContext: ApplicationContext) {
    val rapidMetricsProbe = buildRapidMetricsProbe(environment)
    val varselRepository = VarselRepository(database)
    val eksternVarslingStatusRepository = EksternVarslingStatusRepository(database)
    val eksternVarslingOppdatertProducer = EksternVarslingOppdatertProducer(
        kafkaProducer = initializeRapidKafkaProducer(environment),
        topicName = environment.rapidTopic,
    )

    val eksternVarslingStatusUpdater = EksternVarslingStatusUpdater(eksternVarslingStatusRepository, varselRepository, eksternVarslingOppdatertProducer)

    val varselAktivertProducer = VarselAktivertProducer(
        kafkaProducer = initializeRapidKafkaProducer(environment),
        topicName = environment.rapidTopic,
        rapidMetricsProbe = rapidMetricsProbe
    )

    val varselInaktivertProducer = VarselInaktivertProducer(
        kafkaProducer = initializeRapidKafkaProducer(environment),
        topicName = environment.rapidTopic,
        rapidMetricsProbe = rapidMetricsProbe
    )

    val dbMetricsProbe = buildDBMetricsProbe(environment)
    val doneRepository = DoneRepository(database)
    val periodicDoneEventWaitingTableProcessor =
        PeriodicDoneEventWaitingTableProcessor(doneRepository, varselInaktivertProducer, dbMetricsProbe)

    val expiredMetricsProbe = buildExpiredMetricsProbe(environment)
    val expiredVarselRepository = ExpiredVarselRepository(database)
    val periodicExpiredVarselProcessor =
        PeriodicExpiredVarselProcessor(expiredVarselRepository, varselInaktivertProducer, expiredMetricsProbe)

    RapidApplication.Builder(fromEnv(environment.rapidConfig())).withKtorModule {
        doneApi(
            beskjedRepository = BeskjedRepository(database = database),
            producer = varselInaktivertProducer
        )

    }.build().apply {
        BeskjedSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            varselAktivertProducer = varselAktivertProducer,
            rapidMetricsProbe = rapidMetricsProbe
        )
        OppgaveSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            varselAktivertProducer = varselAktivertProducer,
            rapidMetricsProbe = rapidMetricsProbe
        )
        InnboksSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            varselAktivertProducer = varselAktivertProducer,
            rapidMetricsProbe = rapidMetricsProbe
        )
        DoneSink(
            rapidsConnection = this,
            varselRepository = varselRepository,
            varselInaktivertProducer = varselInaktivertProducer,
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
                periodicDoneEventWaitingTableProcessor.start()
                periodicExpiredVarselProcessor.start()
                appContext.startAllArchivers()
            }

            override fun onShutdown(rapidsConnection: RapidsConnection) {
                runBlocking {
                    periodicDoneEventWaitingTableProcessor.stop()
                    periodicExpiredVarselProcessor.stop()
                    appContext.stopAllArchivers()
                    varselInaktivertProducer.flushAndClose()
                    varselAktivertProducer.flushAndClose()
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
