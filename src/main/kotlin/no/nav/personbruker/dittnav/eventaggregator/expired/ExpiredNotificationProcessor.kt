package no.nav.personbruker.dittnav.eventaggregator.expired

import kotlinx.coroutines.Job

class ExpiredNotificationProcessor(
    private val expiredPersistingService: ExpiredPersistingService,
    private val doneEventEmitter: DoneEventEmitter,
    private val job: Job = Job()
) {

    fun loop() {
        var cursor: Int? = null
        do {
            val beskjeder = expiredPersistingService.getExpiredNotifications(cursor)
            // TODO: skal vi ha beskjeder og oppgaver i seprate processorer?
            cursor = beskjeder.lastOrNull()?.id
            // TODO: add metrics
            // TODO: error handling

            // TODO: hvis den processor kjører på nytt før done eventer er håndtert skal vi sende to Done eventer, kan vi bruke kafka som en table med en konsistent nøkkel for event?
            doneEventEmitter.emittBeskjedDone(beskjeder)
        } while (cursor != null)
    }
}
