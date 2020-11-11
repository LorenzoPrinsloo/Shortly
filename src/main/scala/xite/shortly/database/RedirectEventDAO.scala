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
import xite.shortly.database.tables.RedirectEvent
import xite.shortly.results.{HourlyRedirect, RedirectCounts}

class RedirectEventDAO {

  def insert(id: UUID, url_redirect_id: UUID, created: Timestamp = Timestamp.valueOf(LocalDateTime.now())): ConnectionIO[RedirectEvent] = {
    for {
      _           <- sql"INSERT INTO redirect_events (id, url_redirect_id, created) VALUES ($id, $url_redirect_id, to_timestamp($created, 'YYYY-MM-DD hh24:mi:ss')::timestamp)".update.run
      event       <- sql"SELECT * FROM redirect_events WHERE id = $id".query[RedirectEvent].unique
    } yield event
  }

  def redirectsPerHour(): ConnectionIO[List[HourlyRedirect]] = {
    sql"""SELECT
      date_trunc('hour', created),
      count(1)
      FROM redirect_events
      GROUP BY 1""".query[HourlyRedirect].to[List]
  }

  def redirectsPerHourById(id: UUID): ConnectionIO[List[HourlyRedirect]] = {
    sql"""SELECT
      date_trunc('hour', created),
      count(1)
      FROM redirect_events
      WHERE url_redirect_id = $id
      GROUP BY 1""".query[HourlyRedirect].to[List]
  }

  def redirectsByUrl(): ConnectionIO[List[RedirectCounts]] = {
    sql"""SELECT ur.shortly_identifier, ur.full_url, count(*) FROM redirect_events re
      INNER JOIN url_redirects ur on re.url_redirect_id = ur.id
      GROUP BY ur.shortly_identifier, ur.full_url""".query[RedirectCounts].to[List]
  }

  def redirectByUrl(id: UUID): ConnectionIO[List[RedirectCounts]] = {
    sql"""SELECT ur.shortly_identifier, ur.full_url, count(*) FROM redirect_events re
      INNER JOIN url_redirects ur on re.url_redirect_id = ur.id
      WHERE re.url_redirect_id = $id
      GROUP BY ur.shortly_identifier, ur.full_url""".query[RedirectCounts].to[List]
  }
}
