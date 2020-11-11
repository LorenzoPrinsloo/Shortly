package xite.shortly

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import cats._
import cats.effect._
import cats.implicits._
import doobie.Meta
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

import scala.concurrent.ExecutionContext.Implicits._

object DoobieConfig {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  // A transactor that gets connections from java.sql.DriverManager and executes blocking operations
  // on an our synchronous EC. See the chapter on connection handling for more info.
  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",     // driver classname
    "jdbc:postgresql://localhost:5432/Nigiri",     // connect URL (driver-specific)
    "admin",                  // user
    "admin",                  // password
    Blocker.liftExecutionContext(global) // just for testing
  )

  object meta {
    implicit val metaUUID: Meta[UUID] = Meta[String].imap(string => UUID.fromString(string))(uuid => uuid.toString)
    implicit val metaTimestamp: Meta[Timestamp] = Meta[String].imap(string => Timestamp.valueOf(string))(date => date.toString)
  }
}
