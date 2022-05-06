package no.nav.personbruker.dittnav.eventaggregator.common

import org.amshove.kluent.ExceptionResult
import org.amshove.kluent.`should contain`
import org.amshove.kluent.shouldContain
import java.time.LocalDateTime
import java.time.ZoneOffset

infix fun <T : Throwable> ExceptionResult<T>.`with message containing`(theMessage: String) = this.exceptionMessage shouldContain (theMessage)

infix fun LocalDateTime.`should roughly be equal to`(other: LocalDateTime) {
    val epoch = toEpochSecond(ZoneOffset.UTC)
    val otherEpoch = other.toEpochSecond(ZoneOffset.UTC)

    (otherEpoch - 1..otherEpoch + 1) `should contain` epoch
}
