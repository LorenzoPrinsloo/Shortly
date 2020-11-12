package xite.shortly.database

import java.util.UUID
import doobie.ConnectionIO
import xite.shortly.database.tables.URLRedirect
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import doobie.postgres._
import cats.implicits._
import xite.shortly.DoobieConfig.meta._

class URLRedirectDAO() {
  final case class UnresolvedCollisionError(e: String) extends Exception

  def findOrInsertShortUrl(id: UUID, fullUrl: String)(implicit transactor: Transactor[IO]): IO[URLRedirect] = {
    findOrInsertShortUrlQuery(id, fullUrl)
      .transact(transactor)
  }

  def findOrInsertShortUrlQuery(id: UUID, fullUrl: String): ConnectionIO[URLRedirect] = {

    findById(id)
      .flatMap { existingRedirect =>
        existingRedirect.fold(ifEmpty = insert(id, fullUrl)) { foundRedirect =>
          foundRedirect.pure[ConnectionIO]
        }
      }
  }

  def findById(id: UUID): ConnectionIO[Option[URLRedirect]] = {
    sql"SELECT * FROM url_redirects WHERE id = $id"
      .query[URLRedirect].option
  }

  def findByShortlyIdentifier(shortIdentifier: String)(implicit transactor: Transactor[IO]): IO[Option[URLRedirect]] = {
    findByShortlyIdentifierQuery(shortIdentifier).transact(transactor)
  }

  def findByShortlyIdentifierQuery(shortIdentifier: String): ConnectionIO[Option[URLRedirect]] = {
    sql"SELECT * FROM url_redirects WHERE shortly_identifier = $shortIdentifier"
      .query[URLRedirect].option
  }

  def insert(id: UUID, fullUrl: String, index: Int = 0): ConnectionIO[URLRedirect] = {
    val hashString = id.toString.replace("-","")
    if(index > hashString.length) {
      /** Raise error when recursion reaches end of uuid length, termination case */
      ApplicativeError[ConnectionIO, Throwable]
        .raiseError[URLRedirect](UnresolvedCollisionError("Attempted to Resolve identifier collision but reached end of hash length."))
    } else {
      val shortlyIdentifier = hashString.slice(index, index + 8)
      (for {
        _              <- sql"INSERT INTO url_redirects (id, full_url, shortly_identifier) VALUES ($id, $fullUrl, $shortlyIdentifier)".update.run
        redirect       <- sql"SELECT * FROM url_redirects WHERE id = $id".query[URLRedirect].unique
      } yield redirect)
        .exceptSomeSqlState {
          /** Some Collision happened on our shortly url identifier, recurse until we find one that isn't taken */
          case sqlstate.class23.UNIQUE_VIOLATION => insert(id: UUID, fullUrl, index + 8)
        }
    }
  }

  def list(implicit transactor: Transactor[IO]): IO[List[URLRedirect]] = {
    listQuery.transact(transactor)
  }

  def listQuery: ConnectionIO[List[URLRedirect]] = {
    sql"SELECT * FROM url_redirects"
      .query[URLRedirect]
      .to[List]
  }
}
