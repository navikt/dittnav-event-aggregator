package no.nav.personbruker.dittnav.eventaggregator.common.database.util

import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistOutcome
import java.sql.Array
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDateTime

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

fun ResultSet.getUtcDateTime(columnLabel: String): LocalDateTime = getTimestamp(columnLabel).toLocalDateTime()

fun ResultSet.getNullableLocalDateTime(label: String): LocalDateTime? {
    return getTimestamp(label)?.toLocalDateTime()
}

fun Connection.executeBatchUpdateQuery(sql: String, paramInit: PreparedStatement.() -> Unit) {
    autoCommit = false
    prepareStatement(sql).use { statement ->
        statement.paramInit()
        statement.executeBatch()
    }
    commit()
}

fun Connection.executeBatchPersistQueryIgnoreConflict(sql: String, paramInit: PreparedStatement.() -> Unit): IntArray {
    autoCommit = false
    val result = prepareStatement("""$sql ON CONFLICT DO NOTHING""").use { statement ->
        statement.paramInit()
        statement.executeBatch()
    }
    commit()
    return result
}

fun Connection.executeBatchPersistQuery(sql: String, paramInit: PreparedStatement.() -> Unit): IntArray {
    autoCommit = false
    val result = prepareStatement(sql).use { statement ->
        statement.paramInit()
        statement.executeBatch()
    }
    commit()
    return result
}

fun <T> IntArray.toBatchPersistResult(paramList: List<T>) = ListPersistActionResult.mapParamListToResultArray(paramList, this)

fun Connection.toVarcharArray(stringList: List<String>): Array {
    return createArrayOf("VARCHAR", stringList.toTypedArray())
}

fun Connection.executePersistQuery(sql: String, paramInit: PreparedStatement.() -> Unit): PersistActionResult =
        prepareStatement("""$sql ON CONFLICT DO NOTHING""", Statement.RETURN_GENERATED_KEYS).use {

            it.paramInit()
            it.executeUpdate()

            if (it.generatedKeys.next()) {
                PersistActionResult.success(it.generatedKeys.getInt("id"))
            } else {
                PersistActionResult.failure(PersistOutcome.NO_INSERT_OR_UPDATE)
            }
        }

fun ResultSet.getListFromSeparatedString(columnLabel: String, separator: String): List<String> {
    val stringValue = getString(columnLabel)
    return if(stringValue.isNullOrEmpty()) {
        emptyList()
    }
    else {
        stringValue.split(separator)
    }
}
