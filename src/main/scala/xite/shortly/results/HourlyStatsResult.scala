package xite.shortly.results

import cats.Applicative
import cats.effect.Async
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class HourlyStatsResult(list: List[HourlyRedirect])
object HourlyStatsResult {
  implicit val shortenResultDecoder: Decoder[HourlyStatsResult] = deriveDecoder[HourlyStatsResult]
  implicit def shortenResultEntityDecoder[F[_]: Async]: EntityDecoder[F, HourlyStatsResult] = jsonOf
  implicit val shortenResultEncoder: Encoder[HourlyStatsResult] = deriveEncoder[HourlyStatsResult]
  implicit def shortenResultEntityEncoder[F[_]: Applicative]: EntityEncoder[F, HourlyStatsResult] = jsonEncoderOf
}
