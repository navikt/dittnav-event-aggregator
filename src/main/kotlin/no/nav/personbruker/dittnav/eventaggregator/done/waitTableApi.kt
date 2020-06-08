package no.nav.personbruker.dittnav.eventaggregator.done

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext

fun Routing.waitTableApi(appContext: ApplicationContext) {

    get("/internal/waittable/start") {
        val responseText = "Starter periodisk prosessering av done-tabellen."
        appContext.reinitializeDoneWaitingTableProcessor()
        appContext.periodicDoneEventWaitingTableProcessor.start()
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/waittable/stop") {
        val responseText = "Stoppet periodisk prosessering av done-tabellen."
        appContext.periodicDoneEventWaitingTableProcessor.stop()
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/waittable/process") {
        appContext.periodicDoneEventWaitingTableProcessor.processDoneEvents()
        val responseText = "Manuelt trigget prosessering av ventetabellen for done-eventer."
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

}
