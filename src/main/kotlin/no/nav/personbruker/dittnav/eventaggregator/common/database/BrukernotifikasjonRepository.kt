package no.nav.personbruker.dittnav.eventaggregator.common.database

interface BrukernotifikasjonRepository<T> {

    suspend fun createInOneBatch(entities: List<T>)

    suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<T>)

    suspend fun getTotalNumberOfEvents(): Long
    suspend fun getNumberOfActiveEvents(): Long
    suspend fun getNumberOfInactiveEvents(): Long

}
