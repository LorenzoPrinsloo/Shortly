package xite.shortly.database

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import xite.shortly.DoobieConfig._

object Bootstrap {

  val dropUrlRedirects: ConnectionIO[Int] = sql"DROP TABLE IF EXISTS url_redirects CASCADE".update.run

  val urlRedirects: ConnectionIO[Int] =
    sql"""
    CREATE TABLE url_redirects (
      id   TEXT PRIMARY KEY NOT NULL ,
      full_url TEXT NOT NULL UNIQUE,
      shortly_identifier  TEXT NOT NULL UNIQUE
    )
  """.update.run

  val dropRedirectEvents: ConnectionIO[Int] = sql"DROP TABLE IF EXISTS redirect_events CASCADE".update.run

  val redirectEvents: ConnectionIO[Int] =
    sql"""
    CREATE TABLE redirect_events (
      id   TEXT PRIMARY KEY NOT NULL ,
      url_redirect_id TEXT NOT NULL,
      created  TIMESTAMP DEFAULT NOW() NOT NULL,
      CONSTRAINT fk_url_redirects
        FOREIGN KEY (url_redirect_id)
        REFERENCES url_redirects(id)
    )
  """.update.run

  /**
    * Create Database tables
    * @return
    */
  def createDDL(): Int = {
    (dropUrlRedirects, urlRedirects, dropRedirectEvents, redirectEvents)
      .mapN(_ + _ + _ + _)
      .transact(xa)
      .unsafeRunSync()
  }
}
