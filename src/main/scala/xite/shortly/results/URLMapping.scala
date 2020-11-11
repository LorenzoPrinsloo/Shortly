package xite.shortly.results

import cats.Applicative
import cats.effect.{Async, Sync}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class URLMapping(shortUrl: String, originalUrl: String)
object URLMapping {
  implicit val urlMappingDecoder: Decoder[URLMapping] = deriveDecoder[URLMapping]
  implicit def urlMappingEntityDecoder[F[_]: Async]: EntityDecoder[F, URLMapping] = jsonOf
  implicit val urlMappingEncoder: Encoder[URLMapping] = deriveEncoder[URLMapping]
  implicit def urlMappingEntityEncoder[F[_]: Applicative]: EntityEncoder[F, URLMapping] = jsonEncoderOf
}
