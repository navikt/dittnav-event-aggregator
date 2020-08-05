package no.nav.personbruker.dittnav.eventaggregator.metrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.getProdusentnavn
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

class ProducerNameResolver(private val database: Database) {

    private var producerNameAliases: Map<String, String> = emptyMap()
    private var lastRetrievedFromDB: LocalDateTime? = null
    private val PRODUCERNAME_CACHE_IN_MINUTES = 15

    private val log: Logger = LoggerFactory.getLogger(ProducerNameResolver::class.java)

    suspend fun getProducerNameAlias(systembruker: String): String? {
        updateCacheIfNeeded(systembruker)
        val alias = producerNameAliases[systembruker]
        if (aliasNotFound(alias)) {
            log.warn("Alias for '$systembruker' ble verken funnet i cache eller databasen, mangler det en mapping?")
        }
        return alias
    }

    private fun aliasNotFound(alias: String?) = alias == null

    private suspend fun updateCacheIfNeeded(systembruker: String) {
        if (shouldFetchNewValuesFromDB()) {
            log.info("Periodisk oppdatering av cache.")
            updateCache()

        } else {
            val containsAlias = producerNameAliases.containsKey(systembruker)
            if (!containsAlias) {
                log.info("Manglet alias for '$systembruker', forsøker å oppdatere cache på nytt.")
                updateCache()
            }
        }
    }

    private suspend fun updateCache() = withContext(Dispatchers.IO) {
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

        } catch (e: Exception) {
            log.error("En feil oppstod ved henting av produsentnavn, har ikke oppdatert cache med verdier fra DB.", e)
            producerNameAliases
        }
    }
}

