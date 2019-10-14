package no.nav.personbruker.dittnav.eventaggregator.common.exceptions

open class AbstractPersonbrukerException(message: String, cause: Throwable?) : Exception(message, cause) {

    private val context: MutableMap<String, Any> = mutableMapOf()

    fun addContext(key: String, value: Any) {
        context[key] = value
    }

    override fun toString(): String {
        return when (context.isNotEmpty()) {
            true -> super.toString() + ", context: $context"
            false -> super.toString()
        }
    }

}
