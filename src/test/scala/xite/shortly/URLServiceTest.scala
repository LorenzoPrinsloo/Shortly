package xite.shortly

import java.util.UUID

import cats.arrow.FunctionK
import cats.effect.{Async, IO}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import xite.shortly.database.tables.{RedirectEvent, URLRedirect}
import xite.shortly.database.{RedirectEventDAO, URLRedirectDAO}
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import xite.shortly.URLService.URLError

class URLServiceTest extends AnyFlatSpec with should.Matchers with MockitoSugar with ArgumentMatchersSugar {

  "shorten" should "Generate a unique id based off the url string's bytes and generate a url that points to the current domain" in {
    val urlRedirectDAO: URLRedirectDAO = mock[URLRedirectDAO]
    val redirectEventDAO: RedirectEventDAO = mock[RedirectEventDAO]
    val transactor = mock[Transactor[IO]]

    val urlService = URLService.impl[IO](Async[IO], urlRedirectDAO, redirectEventDAO, transactor)
    val url = "www.google.com"
    val generatedId = UUID.nameUUIDFromBytes(url.getBytes)
    val expectedShortlyId = generatedId.toString.slice(0, 8)

    doReturn(
      IO(
      URLRedirect(
      id = generatedId,
      full_url = url,
      shortly_identifier = expectedShortlyId
    ))).when(urlRedirectDAO)
      .findOrInsertShortUrl(any, any)(any)

      urlService.shorten(url)
        .map(result => assert(result.url == s"localhost:8080/ly/$expectedShortlyId")).unsafeRunSync()
  }

  "redirect" should "URLError if there is no associated URLMapping for the given identifier" in {
    val urlRedirectDAO: URLRedirectDAO = mock[URLRedirectDAO]
    val redirectEventDAO: RedirectEventDAO = mock[RedirectEventDAO]
    val transactor = mock[Transactor[IO]]

    val urlService = URLService.impl[IO](Async[IO], urlRedirectDAO, redirectEventDAO, transactor)

    doReturn(IO(None))
      .when(urlRedirectDAO)
      .findByShortlyIdentifier(any)(any)

    doReturn(IO(mock[RedirectEvent]))
      .when(redirectEventDAO)
      .insert(any, any, any)(any)

    verify(redirectEventDAO, times(0))
      .insert(any, any, any)(any)

    urlService.redirect("qyt2h312")
      .attempt
      .unsafeRunSync()
      .fold(
        {
          case URLError(message) => assert(true)
          case _ => assert(false)
        },
        _ => assert(false)
      )
  }

  "redirect" should "Insert a Redirect Event when URLMapping is found for the given identifier" in {
    val urlRedirectDAO: URLRedirectDAO = mock[URLRedirectDAO]
    val redirectEventDAO: RedirectEventDAO = mock[RedirectEventDAO]
    val transactor = mock[Transactor[IO]]

    val urlService = URLService.impl[IO](Async[IO], urlRedirectDAO, redirectEventDAO, transactor)

    doReturn(IO(Some(URLRedirect(UUID.randomUUID(), "www.google.com", "12345678"))))
      .when(urlRedirectDAO)
      .findByShortlyIdentifier(any)(any)

    doReturn(IO(mock[RedirectEvent]))
      .when(redirectEventDAO)
      .insert(any, any, any)(any)

    val resultMapping = urlService.redirect("12345678")
      .unsafeRunSync()

    assert(resultMapping.originalUrl == "www.google.com")
    assert(resultMapping.shortUrl == "localhost:8080/ly/12345678")
  }
}
