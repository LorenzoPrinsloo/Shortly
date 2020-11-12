package xite.shortly.database

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import doobie.ConnectionIO
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import doobie.postgres._
import cats.implicits._
import xite.shortly.DoobieConfig.meta._
import xite.shortly.DoobieConfig.xa
import xite.shortly.database.tables.RedirectEvent
import xite.shortly.results.{HourlyRedirect, RedirectCounts}

class RedirectEventDAO {

  def insert(
    id: UUID,
    url_redirect_id: UUID,
    created: Timestamp = Timestamp.valueOf(LocalDateTime.now()))(implicit transactor: Transactor[IO]): IO[RedirectEvent] = {
    insertQuery(id, url_redirect_id, created).transact(transactor)
  }

  def insertQuery(id: UUID, url_redirect_id: UUID, created: Timestamp): ConnectionIO[RedirectEvent] = {
    for {
      _           <- sql"INSERT INTO redirect_events (id, url_redirect_id, created) VALUES ($id, $url_redirect_id, to_timestamp($created, 'YYYY-MM-DD hh24:mi:ss')::timestamp)".update.run
      event       <- sql"SELECT * FROM redirect_events WHERE id = $id".query[RedirectEvent].unique
    } yield event
  }

  def redirectsPerHour(maybeId: Option[UUID])(implicit transactor: Transactor[IO]): IO[List[HourlyRedirect]] = {
    maybeId.fold(ifEmpty = redirectsPerHourQuery()) { id =>
      redirectsPerHourByIdQuery(id)
    }.transact(transactor)
  }

  def redirectsPerHourQuery(): ConnectionIO[List[HourlyRedirect]] = {
    sql"""SELECT
      date_trunc('hour', created),
      count(1)
      FROM redirect_events
      GROUP BY 1""".query[HourlyRedirect].to[List]
  }

  def redirectsPerHourByIdQuery(id: UUID): ConnectionIO[List[HourlyRedirect]] = {
    sql"""SELECT
      date_trunc('hour', created),
      count(1)
      FROM redirect_events
      WHERE url_redirect_id = $id
      GROUP BY 1""".query[HourlyRedirect].to[List]
  }

  def redirectsByUrl(maybeId: Option[UUID])(implicit transactor: Transactor[IO]): IO[List[RedirectCounts]] = {
    maybeId.fold(ifEmpty = redirectsByUrlQuery())(id => redirectByUrlQuery(id))
      .transact(transactor)
  }

  def redirectsByUrlQuery(): ConnectionIO[List[RedirectCounts]] = {
    sql"""SELECT ur.shortly_identifier, ur.full_url, count(*) FROM redirect_events re
      INNER JOIN url_redirects ur on re.url_redirect_id = ur.id
      GROUP BY ur.shortly_identifier, ur.full_url""".query[RedirectCounts].to[List]
  }

  def redirectByUrlQuery(id: UUID): ConnectionIO[List[RedirectCounts]] = {
    sql"""SELECT ur.shortly_identifier, ur.full_url, count(*) FROM redirect_events re
      INNER JOIN url_redirects ur on re.url_redirect_id = ur.id
      WHERE re.url_redirect_id = $id
      GROUP BY ur.shortly_identifier, ur.full_url""".query[RedirectCounts].to[List]
  }
}
