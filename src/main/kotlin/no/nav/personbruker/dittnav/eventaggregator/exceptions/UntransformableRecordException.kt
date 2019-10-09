package no.nav.personbruker.dittnav.eventaggregator.exceptions

class UntransformableRecordException(message: String, cause: Throwable?) : AbstractPersonbrukerException(message, cause) {
    constructor(message: String) : this(message, null)
}
