package points.bench

import cats.derived.*
import cats.effect.*
import cats.effect.std.Random
import cats.implicits.*
import cats.{Functor, Monoid}
import com.monovore.decline.*
import com.monovore.decline.effect.*
import fs2.Stream
import points.field.{Field, Player, Pos}

final case class Args(width: Int, height: Int, gamesNumber: Int, seed: Int) derives CanEqual
object Args:
  val width = Opts.option[Int]("width", "Field width.", "w", "WIDTH")
  val height = Opts.option[Int]("height", "Field width.", "h", "HEIGHT")
  val gamesNumber = Opts.option[Int]("games-number", "Games number.", "n", "GAMES")
  val seed = Opts.option[Int]("seed", "RNG seed.", "s", "SEED")
  val args = (width, height, gamesNumber, seed).mapN(Args.apply)

final case class Result(red: Int, black: Int) derives CanEqual, Monoid
object Result:
  def gameResult(field: Field): Result =
    field.winner match
      case Some(Player.Red) => Result(1, 0)
      case Some(Player.Black) => Result(0, 1)
      case None => Result(0, 0)

object Bench extends CommandIOApp("bench", "Points field benchmark"):
  def allMoves(width: Int, height: Int): Seq[Pos] = for
    x <- 0 until width
    y <- 0 until height
  yield Pos(x, y)

  def randomGame[F[_]: Functor: Random](width: Int, height: Int): F[Field] =
    for moves <- Random[F].shuffleList(allMoves(width, height).toList)
    yield moves.foldLeft(Field(width, height))((field, pos) => field.putPoint(pos).getOrElse(field))

  def randomGames[F[_]: Functor: Random](games: Int, width: Int, height: Int): Stream[F, Field] =
    Stream.eval(randomGame(width, height)).repeatN(games)

  override def main: Opts[IO[ExitCode]] =
    Args.args.map(args =>
      for
        given Random[IO] <- Random.scalaUtilRandom[IO]
        result <- randomGames[IO](args.gamesNumber, args.width, args.height)
          .map(Result.gameResult)
          .foldMonoid
          .compile
          .onlyOrError
        _ <- IO.println(s"${result.red}:${result.black}")
      yield ExitCode.Success,
    )
