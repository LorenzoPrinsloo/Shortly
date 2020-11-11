package xite.shortly.requests

import cats.Applicative
import cats.effect.Async
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class ShortenRequest(url: String)
object ShortenRequest {
  implicit val shortenRequestDecoder: Decoder[ShortenRequest] = deriveDecoder[ShortenRequest]
  implicit def shortenRequestEntityDecoder[F[_]: Async]: EntityDecoder[F, ShortenRequest] = jsonOf
  implicit val shortenRequestEncoder: Encoder[ShortenRequest] = deriveEncoder[ShortenRequest]
  implicit def shortenRequestEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ShortenRequest] = jsonEncoderOf
}
