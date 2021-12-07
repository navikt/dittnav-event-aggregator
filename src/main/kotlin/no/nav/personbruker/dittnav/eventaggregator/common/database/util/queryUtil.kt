package no.nav.personbruker.dittnav.eventaggregator.common.database.util

import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import java.sql.*
import java.time.LocalDateTime

const val countResultColumnIndex = 1

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

fun Connection.executeBatchPersistQuery(sql: String, paramInit: PreparedStatement.() -> Unit): IntArray {
    autoCommit = false
    val result = prepareStatement("""$sql ON CONFLICT DO NOTHING""").use { statement ->
        statement.paramInit()
        statement.executeBatch()
    }
    commit()
    return result
}

fun <T> IntArray.toBatchPersistResult(paramList: List<T>) = ListPersistActionResult.mapParamListToResultArray(paramList, this)

inline fun <T> List<T>.persistEachIndividuallyAndAggregateResults(persistAction: (T) -> PersistActionResult): ListPersistActionResult<T> {
    return map { entity ->
        entity to persistAction(entity).persistOutcome
    }.let { aggregate ->
        ListPersistActionResult.mapListOfIndividualResults(aggregate)
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

fun ResultSet.getEpochTimeInSeconds(label: String): Long {
    return getTimestamp(label).toInstant().epochSecond
}

fun Connection.countTotalNumberOfEvents(eventType: EventType): Long {
    val numberOfEvents = prepareStatement("SELECT count(*) from $eventType",
            ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)
            .use { statement ->
                val resultSet = statement.executeQuery()
                resultSet.last()
                resultSet.getLong(countResultColumnIndex)
            }
    return numberOfEvents
}

fun Connection.countTotalNumberOfEventsByActiveStatus(eventType: EventType, aktiv: Boolean): Long {
    val numberOfEvents = prepareStatement("SELECT count(*) from $eventType where aktiv = ?",
            ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)
            .use { statement ->
                statement.setBoolean(1, aktiv)
                val resultSet = statement.executeQuery()
                resultSet.last()
                resultSet.getLong(countResultColumnIndex)
            }
    return numberOfEvents
}

fun ResultSet.getListFromSeparatedString(columnLabel: String, separator: String): List<String> {
    var stringValue = getString(columnLabel)
    return if(stringValue.isNullOrEmpty()) {
        emptyList()
    }
    else {
        stringValue.split(separator)
    }
}
