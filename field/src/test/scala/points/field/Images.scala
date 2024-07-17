package points.field

import cats.data.NonEmptyList
import cats.implicits.*

import scala.jdk.CollectionConverters.*

object Images:
  /** Every letter means a dot that should be placed on the field. Lower-cases are always Red, upper-cases are always
    * Black. Order by which appropriate points are placed: all 'a' points (Red), all 'A' points (Black), all 'b' points
    * (Red), all 'B' points (Black), etc...
    */
  def constructMoveList(image: String): (Int, Int, List[ColoredPos]) =
    val lines = image.stripMargin.lines.nn.toList.nn.asScala.toList.map(_.trim.nn).filter(_.nonEmpty)
    require(lines.groupBy(_.length).size == 1, "lines must have equal length")
    val width = lines.head.length
    val height = lines.size
    val moves = (for
      (line, y) <- lines.zipWithIndex
      (char, x) <- line.zipWithIndex
      if char.toLower != char.toUpper
    yield char -> Pos(x, y))
      .sortBy { case (char, _) =>
        char.toLower -> char.isLower
      }
      .map { case (char, pos) =>
        ColoredPos(pos, Player(char.isLower))
      }
    (width, height, moves)

  def rotations(size: Int): NonEmptyList[Pos => Pos] = NonEmptyList.of[Pos => Pos](
    { case Pos(x, y) => Pos(x, y) },
    { case Pos(x, y) => Pos(size - 1 - x, y) },
    { case Pos(x, y) => Pos(x, size - 1 - y) },
    { case Pos(x, y) => Pos(size - 1 - x, size - 1 - y) },
    { case Pos(x, y) => Pos(y, x) },
    { case Pos(x, y) => Pos(size - 1 - y, x) },
    { case Pos(x, y) => Pos(y, size - 1 - x) },
    { case Pos(x, y) => Pos(size - 1 - y, size - 1 - x) },
  )

  def constructFieldsFromMoves(width: Int, height: Int, moves: List[ColoredPos]): Option[NonEmptyList[Field]] =
    moves.foldLeftM {
      NonEmptyList.one(Field(width, height))
    } { (fields, cp) =>
      fields.head.putPoint(cp.pos, cp.player).map(_ :: fields)
    }

  def constructFieldsFromMovesWithRotations(
    width: Int,
    height: Int,
    moves: List[ColoredPos],
  ): Option[NonEmptyList[(NonEmptyList[Field], Pos => Pos)]] =
    val fieldSize = math.max(width, height)
    rotations(fieldSize).traverse { rotate =>
      val rotatedMoves = moves.map(cp => cp.copy(pos = rotate(cp.pos)))
      constructFieldsFromMoves(fieldSize, fieldSize, rotatedMoves).map(_ -> rotate)
    }

  def surroundings(fields: NonEmptyList[Field]): List[ColoredChain] =
    fields.toList.flatMap(_.lastSurroundChain)

  def constructFields(image: String): Option[NonEmptyList[Field]] =
    constructFieldsFromMoves.tupled(constructMoveList(image))

  def constructLastField(image: String): Option[(Field, List[ColoredChain])] =
    constructFields(image).map { fields =>
      (fields.head, surroundings(fields))
    }

  def constructFieldsWithRotations(image: String): Option[NonEmptyList[(NonEmptyList[Field], Pos => Pos)]] =
    constructFieldsFromMovesWithRotations.tupled(constructMoveList(image))

  def constructLastFieldWithRotations(image: String): Option[NonEmptyList[(Field, List[ColoredChain], Pos => Pos)]] =
    constructFieldsWithRotations(image).map(_.map { case (fields, rotate) =>
      (fields.head, surroundings(fields), rotate)
    })
