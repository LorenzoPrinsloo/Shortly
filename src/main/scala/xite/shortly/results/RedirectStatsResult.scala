package xite.shortly.results

import cats.Applicative
import cats.effect.Async
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class RedirectStatsResult(stats: List[RedirectCounts])
object RedirectStatsResult {
  implicit val shortenResultDecoder: Decoder[RedirectStatsResult] = deriveDecoder[RedirectStatsResult]
  implicit def shortenResultEntityDecoder[F[_]: Async]: EntityDecoder[F, RedirectStatsResult] = jsonOf
  implicit val shortenResultEncoder: Encoder[RedirectStatsResult] = deriveEncoder[RedirectStatsResult]
  implicit def shortenResultEntityEncoder[F[_]: Applicative]: EntityEncoder[F, RedirectStatsResult] = jsonEncoderOf
}
