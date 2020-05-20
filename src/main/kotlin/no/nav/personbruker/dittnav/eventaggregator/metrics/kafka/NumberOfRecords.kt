package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

data class NumberOfRecords(var beskjed: Long = 0,
                           var innboks: Long = 0,
                           var oppgaver: Long = 0,
                           var done: Long = 0) {
    override fun toString(): String {
        val totalt = beskjed + innboks + oppgaver + done
        return "NumberOfRecords(beskjed=$beskjed, innboks=$innboks, oppgaver=$oppgaver, done=$done, totalt=$totalt)"
    }

}
