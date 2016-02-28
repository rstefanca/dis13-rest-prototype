package cz.codingmonkeys.dis13.utils

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait DbConfig {


  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("database")
  val db = dbConfig.db

  import dbConfig.driver.api._

  //implicit val session: slick.backend.Session = db.createSession()
}
