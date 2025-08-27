package points.field

import org.scalacheck.*
import org.scalacheck.Prop.*

import scala.util.Random

class RandomGameFieldSpec extends munit.ScalaCheckSuite:
  val minSize = 3
  val maxSize = 50
  val lengthGen = Gen.choose(minSize, maxSize)
  val seedGen = Arbitrary.arbitrary[Int]

  property("random game"):
    Prop.forAll(lengthGen, lengthGen, seedGen): (width: Int, height: Int, seed: Int) =>
      val random = new Random(seed)
      val moves = random.shuffle((0 until width * height).toVector).map(idx => Pos(idx % width, idx / width))
      val field = Field(width, height)
      val finalField = moves.foldLeft(field)((acc, pos) => acc.putPoint(pos).getOrElse(acc))
      all(
        (finalField.scoreRed >= 0) :| "red score should be non-negative",
        (finalField.scoreBlack >= 0) :| "black score should be non-negative",
        ((field.scoreRed - field.scoreBlack).abs < width * height / 2) :| "score difference should be less than number of player moves",
        (field.scoreRed + field.scoreBlack <= (width - 2) * (height - 2)) :| "full score should be less than field size",
      )
