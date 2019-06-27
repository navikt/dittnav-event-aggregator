package no.nav.personbruker.dittnav.eventaggregator.config

import org.flywaydb.core.Flyway

object Flyway {

    fun runFlywayMigrations(env: Environment) {
        val flyway = Flyway.configure().dataSource(env.dbUrl, env.dbUser, env.dbPassword).load()
        flyway.migrate()
    }

}
