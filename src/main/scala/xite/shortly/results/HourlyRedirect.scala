package xite.shortly.results

import java.sql.Timestamp

import cats.Applicative
import cats.effect.Async
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class HourlyRedirect(hour: Timestamp, redirects: Int)
object HourlyRedirect {
  implicit val shortenResultDecoder: Decoder[HourlyRedirect] = deriveDecoder[HourlyRedirect]
  implicit def shortenResultEntityDecoder[F[_]: Async]: EntityDecoder[F, HourlyRedirect] = jsonOf
  implicit val shortenResultEncoder: Encoder[HourlyRedirect] = deriveEncoder[HourlyRedirect]
  implicit def shortenResultEntityEncoder[F[_]: Applicative]: EntityEncoder[F, HourlyRedirect] = jsonEncoderOf

  implicit val timestampEncoder: Encoder[Timestamp] = Encoder[String].contramap[Timestamp](timestamp => timestamp.toString)
  implicit val timestampDecoder: Decoder[Timestamp] = Decoder[String].map(string => Timestamp.valueOf(string))
}
