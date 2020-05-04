package no.nav.personbruker.dittnav.eventaggregator.metrics

class ProducerNameScrubber(private val producerNameResolver: ProducerNameResolver) {

    val UNKNOWN_USER = "unknown-user"
    val GENERIC_SYSTEM_USER = "unmapped-system-user"

    suspend fun getPublicAlias(producerName: String): String {
        val producerNameAliases = producerNameResolver.getProducerNameAliasesFromCache()
        return producerNameAliases[producerName] ?: findFallBackAlias(producerName)
    }

    private fun findFallBackAlias(producerName: String): String {
        return if (isSystemUser(producerName)) {
            GENERIC_SYSTEM_USER
        } else {
            UNKNOWN_USER
        }
    }

    private fun isSystemUser(producer: String): Boolean {
        return "^srv.{1,12}\$".toRegex().matches(producer)
    }
}
