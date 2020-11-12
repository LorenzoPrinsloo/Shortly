package xite.shortly


import java.net.URL
import java.sql.Timestamp
import java.util.{Properties, UUID}
import cats._
import cats.effect._
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import doobie.Meta
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import scala.concurrent.ExecutionContext.Implicits._
import scala.io.Source

object DoobieConfig {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  val config: Config = ConfigFactory.load("application.conf")

  lazy val url: String = config.getString("db.url")
  lazy val username: String = config.getString("db.username")
  lazy val password: String = config.getString("db.password")

  // A transactor that gets connections from java.sql.DriverManager and executes blocking operations
  // on an our synchronous EC. See the chapter on connection handling for more info.

  implicit val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",     // driver classname
    url,
    username,
    password,
    Blocker.liftExecutionContext(global) // just for testing
  )

  object meta {
    implicit val metaUUID: Meta[UUID] = Meta[String].imap(string => UUID.fromString(string))(uuid => uuid.toString)
    implicit val metaTimestamp: Meta[Timestamp] = Meta[String].imap(string => Timestamp.valueOf(string))(date => date.toString)
  }
}
