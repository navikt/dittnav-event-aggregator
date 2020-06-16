package no.nav.personbruker.dittnav.eventaggregator.common.database

interface BrukernotifikasjonRepository<T> {

    suspend fun createInOneBatch(entities: List<T>): ListPersistActionResult<T>

    suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<T>): ListPersistActionResult<T>

}
