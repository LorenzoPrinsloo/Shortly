package xite.shortly.database.tables

import java.util.UUID
import cats._, cats.data._, cats.implicits._
import doobie._, doobie.implicits._
import io.circe._, io.circe.jawn._, io.circe.syntax._
import org.postgresql.util.PGobject
import xite.shortly.DoobieConfig.meta._

case class URLRedirect(id: UUID, full_url: String, shortly_identifier: String)
