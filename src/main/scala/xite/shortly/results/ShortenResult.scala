package xite.shortly.results

import java.util.UUID

import cats.Applicative
import cats.effect.{Async, Sync}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class ShortenResult(id: UUID, url: String)
object ShortenResult {
  implicit val shortenResultDecoder: Decoder[ShortenResult] = deriveDecoder[ShortenResult]
  implicit def shortenResultEntityDecoder[F[_]: Async]: EntityDecoder[F, ShortenResult] = jsonOf
  implicit val shortenResultEncoder: Encoder[ShortenResult] = deriveEncoder[ShortenResult]
  implicit def shortenResultEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ShortenResult] = jsonEncoderOf
}
