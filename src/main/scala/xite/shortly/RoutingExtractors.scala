package xite.shortly

import java.util.UUID

import org.http4s.QueryParamDecoder
import org.http4s.dsl.io.OptionalQueryParamDecoderMatcher

trait RoutingExtractors {
  implicit val uuidQueryParamDecoder: QueryParamDecoder[UUID] = QueryParamDecoder[String].map(UUID.fromString)

  object OptionalIdExtractor extends OptionalQueryParamDecoderMatcher[UUID]("id")

}
