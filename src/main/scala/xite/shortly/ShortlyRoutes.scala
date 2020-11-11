package xite.shortly

import cats.effect.Async
import cats.implicits._
import org.http4s.{Header, HttpRoutes, QueryParamDecoder}
import org.http4s.dsl.Http4sDsl
import xite.shortly.requests.ShortenRequest
import java.util.UUID

object ShortlyRoutes extends RoutingExtractors {

  def urlRoutes[F[_]: Async](URLService: URLService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of[F] {

      case req @ POST -> Root / "shorten" =>
        for {
          request <- req.as[ShortenRequest]
          shortenResult <- URLService.shorten(request.url)
          resp <- Ok(shortenResult)
        } yield resp

      case GET -> Root / "list" =>

        for {
          mappingList <- URLService.list
          resp <- Ok(mappingList)
        } yield resp

      case GET -> Root / "ly" / shortUrl =>

        for {
          urlMapping <- URLService.redirect(shortUrl)
          resp <- TemporaryRedirect(urlMapping, Header("Location", urlMapping.originalUrl))
        } yield resp

      case GET -> Root / "stats" :? OptionalIdExtractor(id) =>

        for {
          stats <- URLService.redirectStats(id)
          resp  <- Ok(stats)
        } yield resp

      case GET -> Root / "stats" / "hourly" :? OptionalIdExtractor(id) =>

        for {
          stats <- URLService.hourlyRedirectStats(id)
          resp  <- Ok(stats)
        } yield resp


    }
  }
}
