package xite.shortly.results

import cats.Applicative
import cats.effect.Async
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class ListResult(mappings: List[URLMapping])
object ListResult {
  implicit val listResultDecoder: Decoder[ListResult] = deriveDecoder[ListResult]
  implicit def listResultEntityDecoder[F[_]: Async]: EntityDecoder[F, ListResult] = jsonOf
  implicit val listResultEncoder: Encoder[ListResult] = deriveEncoder[ListResult]
  implicit def listResultEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ListResult] = jsonEncoderOf
}
