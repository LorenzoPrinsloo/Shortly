package xite.shortly

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import xite.shortly.database.Bootstrap

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    Bootstrap.createDDL()
    ShortlyServer.stream[IO].compile.drain.as(ExitCode.Success)
  }
}
