package no.nav.personbruker.dittnav.eventaggregator.common.exceptions

class ConstraintViolationDatabaseException(message: String, cause: Throwable?) : RetriableDatabaseException(message, cause) {
    constructor(message: String) : this(message, null)
}
