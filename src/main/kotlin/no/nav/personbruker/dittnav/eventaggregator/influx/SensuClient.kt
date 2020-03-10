package no.nav.personbruker.dittnav.eventaggregator.influx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets.UTF_8

class SensuClient (val hostname: String, val port: Int) {

    val log = LoggerFactory.getLogger(SensuClient::class.java)

    suspend fun submitEvent(event: SensuEvent) = withContext(Dispatchers.IO) {
        try {
            val socket = Socket(hostname, port)

            OutputStreamWriter(socket.getOutputStream(), UTF_8).also { writer ->
                writer.write(event.toJson())
                writer.flush()
                log.debug("Sendte metrics event til [$hostname:$port]. Payload: [${event.toJson()}]")
            }
        } catch (ioe: IOException) {
            log.warn("Kunne ikke sende metrics event til [$hostname:$port]. Payload: [${event.toJson()}].", ioe)
        } catch (uhe: UnknownHostException) {
            log.warn("Kunne ikke koble til sensu host [$hostname:$port]")
        } catch (e: Exception) {
            log.warn("Kunne ikke sende metrics event til [$hostname:$port]. Payload: [${event.toJson()}].", e)
        }
    }
}