package no.nav.personbruker.dittnav.eventaggregator.common.exceptions

class UntransformableRecordException(message: String, cause: Throwable?) : AbstractPersonbrukerException(message, cause) {
    constructor(message: String) : this(message, null)
}
