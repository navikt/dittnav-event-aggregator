package no.nav.personbruker.dittnav.eventaggregator.common.exceptions

class AggregatorBatchUpdateException(message: String, cause: Throwable?) : RetriableDatabaseException(message, cause) {
    constructor(message: String) : this(message, null)
}
