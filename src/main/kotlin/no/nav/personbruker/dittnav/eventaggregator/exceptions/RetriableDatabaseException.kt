package no.nav.personbruker.dittnav.eventaggregator.exceptions

class RetriableDatabaseException(message: String, cause: Throwable?) : AbstractPersonbrukerException(message, cause) {
    constructor(message: String) : this(message, null)
}
