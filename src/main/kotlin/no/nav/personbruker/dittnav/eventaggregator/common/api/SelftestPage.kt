package no.nav.personbruker.dittnav.eventaggregator.common.api

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.html.*
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.HttpClientBuilder
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka.doneTopicName

suspend fun ApplicationCall.pingDependencies(appContext: ApplicationContext) = coroutineScope {
    val client = HttpClientBuilder.build()

    val dataSourceSelftestStatus = async { getDatasourceConnectionStatus(appContext.database) }
    val kafkaSelftestStatus = async { getKafkaHealthStatusOnSpecificTopic(appContext, EventType.DONE, doneTopicName) }

    val services =
            mapOf(
                    "DATABASE:" to dataSourceSelftestStatus.await()
                    , "KAFKA:" to kafkaSelftestStatus.await()
            )

    client.close()

    val serviceStatus = if (services.values.any { it.status == Status.ERROR }) Status.ERROR else Status.OK

    respondHtml {
        head {
            title { +"Selftest dittnav-event-aggregator" }
        }
        body {
            h1 {
                style = if (serviceStatus == Status.OK) "background: green" else "background: red;font-weight:bold"
                +"Service status: $serviceStatus"
            }
            table {
                thead {
                    tr { th { +"SELFTEST DITTNAV-EVENT-AGGREGATOR" } }
                }
                tbody {
                    services.map {
                        tr {
                            td { +it.key }
                            td {
                                style = if (it.value.status == Status.OK) "background: green" else "background: red;font-weight:bold"
                                +it.value.status.toString()
                            }
                            td { +it.value.statusMessage }
                        }
                    }
                }
            }
        }
    }
}
