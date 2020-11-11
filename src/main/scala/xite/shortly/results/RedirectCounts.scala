package xite.shortly.results

import cats.Applicative
import cats.effect.Async
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class RedirectCounts(shortlyUrl: String, fullURL: String, redirects: Int)
object RedirectCounts {
  implicit val shortenResultDecoder: Decoder[RedirectCounts] = deriveDecoder[RedirectCounts]
  implicit def shortenResultEntityDecoder[F[_]: Async]: EntityDecoder[F, RedirectCounts] = jsonOf
  implicit val shortenResultEncoder: Encoder[RedirectCounts] = deriveEncoder[RedirectCounts]
  implicit def shortenResultEntityEncoder[F[_]: Applicative]: EntityEncoder[F, RedirectCounts] = jsonEncoderOf
}
