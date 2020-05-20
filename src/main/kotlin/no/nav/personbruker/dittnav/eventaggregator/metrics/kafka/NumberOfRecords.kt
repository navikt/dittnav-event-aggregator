package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

data class NumberOfRecords(var beskjed: Long = -1,
                           var innboks: Long = -1,
                           var oppgaver: Long = -1,
                           var done: Long = -1) {
    override fun toString(): String {
        val totalt = beskjed + innboks + oppgaver + done
        return "NumberOfRecords(beskjed=$beskjed, innboks=$innboks, oppgaver=$oppgaver, done=$done, totalt=$totalt)"
    }

}
