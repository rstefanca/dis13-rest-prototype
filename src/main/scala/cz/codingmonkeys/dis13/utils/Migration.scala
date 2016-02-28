package cz.codingmonkeys.dis13.utils

import org.flywaydb.core.Flyway

trait Migration extends Config {

  private val flyway = new Flyway()
  flyway.setDataSource(databaseUrl, databaseUser, databasePassword)
  flyway.setBaselineOnMigrate(true)

  def migrate() = {
    flyway.migrate()
  }

  def reloadSchema() = {
    flyway.clean()
    flyway.migrate()
  }

}
