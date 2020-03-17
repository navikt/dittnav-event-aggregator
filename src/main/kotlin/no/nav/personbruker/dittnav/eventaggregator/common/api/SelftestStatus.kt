package no.nav.personbruker.dittnav.eventaggregator.common.api


import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka.doneTopicName
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.AuthenticationException
import org.apache.kafka.common.errors.InterruptException
import org.apache.kafka.common.errors.TimeoutException
import org.slf4j.LoggerFactory
import java.sql.SQLException

enum class Status {
    OK, ERROR
}

data class SelftestStatus(val status: Status, val statusMessage: String)

private val log = LoggerFactory.getLogger(SelftestStatus::class.java)

fun getDatasourceConnectionStatus(database: Database): SelftestStatus {
    return try {
        database.dataSource.getConnection()
        SelftestStatus(Status.OK, "200 OK")
    } catch (e: SQLException) {
        log.error("HikariDataSource er lukket, vi får derfor en feil mot event-cachen.", e)
        SelftestStatus(Status.ERROR, "Feil mot DB")
    } catch (e: Exception) {
        log.error("Vi får en uventet feil mot event-cachen. Connection failed.", e)
        SelftestStatus(Status.ERROR, "Feil mot DB")
    }
}

fun getKafkaHealthStatusOnSpecificTopic(appContext: ApplicationContext, eventType: EventType, topicName: String): SelftestStatus {
    return try {
        KafkaConsumer<Nokkel, Done>(Kafka.consumerProps(appContext.environment, eventType)).use { consumer ->
            consumer.partitionsFor(topicName)
        }
        SelftestStatus(Status.OK, "200 OK")
    } catch (e: AuthenticationException) {
        log.error("SelftestStatus klarte ikke å autentisere seg mot Kafka. TopicName: ${doneTopicName}", e)
        SelftestStatus(Status.ERROR, "Feil mot Kafka")
    } catch (e: InterruptException) {
        log.error("Fikk feil mot Kafka. TopicName: ${doneTopicName}", e)
        SelftestStatus(Status.ERROR, "Feil mot Kafka")
    } catch (e: TimeoutException) {
        log.error("Noe gikk galt, vi fikk timeout mot Kafka. TopicName: ${doneTopicName}", e)
        SelftestStatus(Status.ERROR, "Feil mot Kafka")
    } catch (e: KafkaException) {
        log.error("Fikk en feil mot Kafka. TopicName: ${doneTopicName}", e)
        SelftestStatus(Status.ERROR, "Feil mot Kafka")
    }
}