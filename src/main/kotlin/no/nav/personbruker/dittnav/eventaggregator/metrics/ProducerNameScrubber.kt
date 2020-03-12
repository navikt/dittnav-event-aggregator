package no.nav.personbruker.dittnav.eventaggregator.metrics

class ProducerNameScrubber(producerAliasesString: String) {

    private val producerNameAliases: Map<String, String> = parseStringAsMap(producerAliasesString)

    val GENERIC_NON_SYSTEM_USER = "non-system-user"
    val GENERIC_SYSTEM_USER = "unmapped-system-user"

    fun getPublicAlias(producerName: String): String {
        return if (isNonSystemUser(producerName)) {
            GENERIC_NON_SYSTEM_USER
        } else {
            producerNameAliases[producerName]?: GENERIC_SYSTEM_USER
        }
    }

    private fun parseStringAsMap(varString: String): Map<String, String> {
        return varString.split(",")
                .map { keyValString -> keyValString.split(":") }
                .filter { keyValPair -> keyValPair.size == 2 }
                .map { keyValPair -> keyValPair[0] to keyValPair[1] }
                .toMap()
    }

    private fun isNonSystemUser(producer: String): Boolean {
        return "^[a-zA-Z][0-9]{6}\$".toRegex().matches(producer)
    }
}

