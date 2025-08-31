package points.verify

import cats.effect.std.Console
import cats.effect.{IO, IOApp}
import cats.implicits.*
import fs2.Chunk
import points.field.{Field, Pos}

object Verify extends IOApp.Simple:
  override val run =
    val width = 20
    val height = 20
    val initField = Field(width, height)
    fs2.io
      .stdinUtf8[IO](32)
      .repartition(s => Chunk.array(s.split("\n", -1)))
      .evalMap:
        case "" => none[Pos].pure[IO]
        case s =>
          s.trim.split(" ") match
            case Array(x, y) =>
              val pos = for
                x <- x.toIntOption
                y <- y.toIntOption
              yield Pos(x, y)
              if pos.isEmpty then IO.raiseError(new RuntimeException("wrong input"))
              else pos.pure[IO]
            case _ => IO.raiseError(new RuntimeException("wrong input"))
      .evalFold(initField):
        case (field, None) =>
          (0 until width * height).toVector.map(idx => Pos(idx % width, idx / width)).find(field.isPuttingAllowed) match
            case Some(pos) => IO.raiseError(new RuntimeException(s"field is not fully occupied: ${pos}"))
            case None => Console[IO].println("").as(initField)
        case (field, Some(pos)) =>
          field
            .putPoint(pos)
            .map: field =>
              Console[IO].println(s"${field.scoreRed} ${field.scoreBlack}").as(field)
            .getOrElse(IO.raiseError(new RuntimeException("invalid position")))
      .compile
      .drain
