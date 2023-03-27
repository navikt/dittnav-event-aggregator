package no.nav.personbruker.dittnav.eventaggregator.common.database

open class AbstractPersonbrukerException(message: String, cause: Throwable?) : Exception(message, cause) {

    val context: MutableMap<String, Any> = mutableMapOf()

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

class UnretriableDatabaseException(message: String, cause: Throwable?) : AbstractPersonbrukerException(message, cause)

open class RetriableDatabaseException(message: String, cause: Throwable?) : AbstractPersonbrukerException(message, cause)

class AggregatorBatchUpdateException(message: String, cause: Throwable?) : RetriableDatabaseException(message, cause) {
    constructor(message: String) : this(message, null)
}
