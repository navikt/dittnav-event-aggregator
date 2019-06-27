package no.nav.personbruker.dittnav.eventaggregator.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

object Flyway {

    fun runFlywayMigrations(env: Environment) {
        val flyway = configure(env).load()
        flyway.migrate()
    }

    private fun configure(env: Environment): FluentConfiguration {
        val configBuilder = Flyway.configure()
        val dataSource = DatabaseConnectionFactory.createCorrectDatasourceForEnvironment(env)
        configBuilder.dataSource(dataSource)

        if (ConfigUtil.isCurrentlyRunningOnNais()) {
            configBuilder.initSql("SET ROLE \"${env.dbAdmin}\"")
        }
        return configBuilder
    }

}
