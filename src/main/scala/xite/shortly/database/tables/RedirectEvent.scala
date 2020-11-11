package xite.shortly.database.tables

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

case class RedirectEvent(id: UUID, url_redirect_id: UUID, created: Timestamp)
