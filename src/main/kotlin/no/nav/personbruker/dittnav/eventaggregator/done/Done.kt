package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.validation.validateFodselsnummer
import no.nav.personbruker.dittnav.eventaggregator.common.validation.validateNonNullFieldMaxLength
import java.time.LocalDateTime

data class Done(
        val produsent: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String
) {

    init {
        validateNonNullFieldMaxLength(produsent, "produsent", 100)
        validateNonNullFieldMaxLength(eventId, "eventId", 50)
        validateFodselsnummer(fodselsnummer)
    }

    override fun toString(): String {
        return "Done(" +
                "produsent=$produsent, " +
                "eventId=$eventId, " +
                "eventTidspunkt=$eventTidspunkt, " +
                "fodselsnummer=***, " +
                "grupperingsId=$grupperingsId"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Done

        if (produsent != other.produsent) return false
        if (eventId != other.eventId) return false
        if (eventTidspunkt != other.eventTidspunkt) return false
        if (fodselsnummer != other.fodselsnummer) return false
        if (grupperingsId != other.grupperingsId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = produsent.hashCode()
        result = 31 * result + eventId.hashCode()
        result = 31 * result + eventTidspunkt.hashCode()
        result = 31 * result + fodselsnummer.hashCode()
        result = 31 * result + grupperingsId.hashCode()
        return result
    }

}
