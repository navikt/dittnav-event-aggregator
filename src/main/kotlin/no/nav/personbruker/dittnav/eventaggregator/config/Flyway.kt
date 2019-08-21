package no.nav.personbruker.dittnav.eventaggregator.config

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import javax.sql.DataSource

object Flyway {

    fun runFlywayMigrations(env: Environment) {
        val flyway = configure(env).load()
        flyway.migrate()
    }

    private fun configure(env: Environment): FluentConfiguration {
        val configBuilder = Flyway.configure()
        val dataSource = createCorrectAdminDatasourceForEnvironment(env)
        configBuilder.dataSource(dataSource)

        if (ConfigUtil.isCurrentlyRunningOnNais()) {
            configBuilder.initSql("SET ROLE \"${env.dbAdmin}\"")
        }
        return configBuilder
    }

    private fun createCorrectAdminDatasourceForEnvironment(env: Environment): DataSource {
        return when (ConfigUtil.isCurrentlyRunningOnNais()) {
            true -> createDataSourceViaVaultWithAdminUser(env)
            false -> createDataSourceForLocalDbWithAdminUser(env)
        }
    }

    private fun createDataSourceViaVaultWithAdminUser(env: Environment): HikariDataSource {
        return Database.hikariDatasourceViaVault(env, env.dbAdmin)
    }

    private fun createDataSourceForLocalDbWithAdminUser(env: Environment): HikariDataSource {
        return Database.hikariFromLocalDb(env, env.dbUser)
    }

}
