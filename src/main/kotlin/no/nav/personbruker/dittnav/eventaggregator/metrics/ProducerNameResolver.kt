package no.nav.personbruker.dittnav.eventaggregator.metrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.getProdusentnavn
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.Duration
import java.time.LocalDateTime

class ProducerNameResolver(private val database: Database) {

    private var producerNameAliases: Map<String, String> = emptyMap()
    private var lastRetrievedFromDB: LocalDateTime? = null
    private val PRODUCERNAME_CACHE_IN_MINUTES = 15

    private val log: Logger = LoggerFactory.getLogger(ProducerNameResolver::class.java)

    suspend fun getProducerNameAlias(systembruker: String): String? {
        if(shouldFetchNewValuesFromDB()) {
            withContext(Dispatchers.IO) {
                updateCache()
            }
        }
        var producerNameAlias = producerNameAliases[systembruker]
        if(producerNameAlias == null) {
            log.warn("Mangler alias for oppgitt systembruker, forsøker å oppdatere cache på nytt.")
            updateCache()
        }
        return producerNameAlias
    }

    private suspend fun updateCache() {
        producerNameAliases = populateProducerNameCache()
        lastRetrievedFromDB = LocalDateTime.now()
    }

    private fun shouldFetchNewValuesFromDB(): Boolean {
        return producerNameAliases.isEmpty() ||
                lastRetrievedFromDB == null ||
                Math.abs(Duration.between(lastRetrievedFromDB, LocalDateTime.now()).toMinutes()) > PRODUCERNAME_CACHE_IN_MINUTES
    }

    private suspend fun populateProducerNameCache(): Map<String, String> {
        return try {
            val producers = database.queryWithExceptionTranslation { getProdusentnavn() }
            producers.map { it.systembruker to it.produsentnavn }.toMap()
        } catch(e: Exception) {
            log.error("En feil oppstod ved henting av produsentnavn, har ikke oppdatert cache med verdier fra DB.", e)
            producerNameAliases
        }
    }
}

