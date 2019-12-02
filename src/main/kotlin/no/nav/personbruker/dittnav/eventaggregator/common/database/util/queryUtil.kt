package no.nav.personbruker.dittnav.eventaggregator.common.database.util

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import java.sql.*

fun <T> ResultSet.singleResult(result: ResultSet.() -> T): T =
        if (next()) {
            result()
        } else {
            throw SQLException("Found no rows")
        }

fun <T> ResultSet.list(result: ResultSet.() -> T): List<T> =
        mutableListOf<T>().apply {
            while (next()) {
                add(result())
            }
        }

fun Connection.executePersistQuery(sql: String, paramInit: PreparedStatement.() -> Unit): PersistActionResult =
        prepareStatement("""$sql ON CONFLICT DO NOTHING""", Statement.RETURN_GENERATED_KEYS).use {

            it.paramInit()
            it.executeUpdate()

            if (it.generatedKeys.next()) {
                PersistActionResult.success(it.generatedKeys.getInt("id"))
            } else {
                PersistActionResult.failure(PersistFailureReason.CONFLICTING_KEYS)
            }
        }