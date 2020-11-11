package xite.shortly

import cats.Applicative
import cats.effect.{Async, IO, Sync}
import cats.implicits._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder, Method, Request, Uri}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import DoobieConfig._
import org.http4s.Method._
import org.http4s.circe._
import xite.shortly.results.{HourlyStatsResult, ListResult, RedirectStatsResult, ShortenResult, URLMapping}
import java.util.UUID
import doobie.implicits._
import xite.shortly.database.{RedirectEventDAO, URLRedirectDAO}

trait URLService[F[_]]{
  def shorten(url: String): F[ShortenResult]
  def list: F[ListResult]
  def redirectStats(maybeId: Option[UUID]): F[RedirectStatsResult]
  def hourlyRedirectStats(maybeId: Option[UUID]): F[HourlyStatsResult]
  def redirect(shortlyIdentifier: String): F[URLMapping]
}

object URLService {
  def apply[F[_]](implicit ev: URLService[F]): URLService[F] = ev

  final case class URLError(e: String) extends Exception

  lazy val urlRedirectDAO = new URLRedirectDAO()
  lazy val redirectEventDAO = new RedirectEventDAO()

  lazy val domain: String = "localhost:8080/ly/"

  def impl[F[_]: Async](C: Client[F]): URLService[F] = new URLService[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._

    private def shortUrl(shortlyIdentifier: String): String = s"$domain${shortlyIdentifier}"

    def shorten(url: String): F[ShortenResult] = {
      val generatedHash = UUID.nameUUIDFromBytes(url.getBytes)

      Async.liftIO(
        urlRedirectDAO.findOrInsertShortUrl(generatedHash, url)
          .transact(xa)
          .map(redirect => ShortenResult(redirect.id, shortUrl(redirect.shortly_identifier)))
      )(Async[F])
    }

    def list: F[ListResult] = {
      Async.liftIO(
        urlRedirectDAO.list
          .transact(xa)
          .map { redirects =>
            redirects.map { redirect =>
              URLMapping(shortUrl(redirect.shortly_identifier), redirect.full_url)
            }
          }.map(mappings => ListResult(mappings))
      )(Async[F])
    }

    def redirectStats(maybeId: Option[UUID]): F[RedirectStatsResult] = {
      Async.liftIO(
        maybeId.fold(ifEmpty = redirectEventDAO.redirectsByUrl()) { id =>
          redirectEventDAO.redirectByUrl(id)
        }.transact(xa)
          .map(RedirectStatsResult.apply)
      )(Async[F])
    }

    def hourlyRedirectStats(maybeId: Option[UUID]): F[HourlyStatsResult] = {
      Async.liftIO(
        maybeId.fold(ifEmpty = redirectEventDAO.redirectsPerHour()) { id =>
          redirectEventDAO.redirectsPerHourById(id)
        }.transact(xa)
          .map(HourlyStatsResult.apply)
      )(Async[F])
    }

    def redirect(shortlyIdentifier: String): F[URLMapping] = {
      Async.liftIO(
        urlRedirectDAO.findByShortlyIdentifier(shortlyIdentifier)
          .transact(xa)
      )(Async[F])
        .flatMap { maybeRedirect =>
        maybeRedirect.fold(ifEmpty = Async[F].raiseError[URLMapping](URLError("No available shortly url redirect."))) { redirect =>
          Async.liftIO(
            redirectEventDAO.insert(UUID.randomUUID(), redirect.id)
              .transact(xa)
              .map(_ => URLMapping(shortUrl(redirect.shortly_identifier), redirect.full_url))
          )(Async[F])
        }
      }
    }
  }
}
