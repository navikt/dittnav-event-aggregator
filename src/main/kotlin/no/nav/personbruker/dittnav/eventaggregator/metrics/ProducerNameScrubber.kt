package no.nav.personbruker.dittnav.eventaggregator.metrics

class ProducerNameScrubber(private val producerNameResolver: ProducerNameResolver) {

    val UNKNOWN_USER = "unknown-user"
    val GENERIC_SYSTEM_USER = "unmapped-system-user"

    fun getPublicAlias(producerName: String): String {
        val producerNameAliases = producerNameResolver.getProducerNameAliases()
        return producerNameAliases[producerName] ?: findFallBackAlias(producerName)
    }

    private fun parseStringAsMap(varString: String): Map<String, String> {
        return varString.split(",")
                .map { keyValString -> keyValString.split(":") }
                .filter { keyValPair -> keyValPair.size == 2 }
                .map { keyValPair -> keyValPair[0] to keyValPair[1] }
                .toMap()
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
