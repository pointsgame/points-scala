package points.field

import cats.implicits.*

final case class Vector2D[T] private (width: Int, vector: Vector[T]) extends Iterable[T]:
  val height: Int = vector.size / width

  private def toIndex(x: Int, y: Int): Int = y * width + x
  private def toX(idx: Int): Int = idx % width
  private def toY(idx: Int): Int = idx / width

  def apply(x: Int, y: Int): T = vector(toIndex(x, y))

  def get(x: Int, y: Int): Option[T] =
    if x >= 0 && x < width && y >= 0 && y < height
    then vector.get(toIndex(x, y))
    else none

  override def iterator: Iterator[T] =
    vector.iterator

  override def map[U](f: T => U): Vector2D[U] = Vector2D(width, vector.map(f))

  def mapWithIndex[U](f: (Int, Int, T) => U): Vector2D[U] =
    Vector2D(
      width,
      vector.zipWithIndex.map { case (value, idx) =>
        f(toX(idx), toY(idx), value)
      },
    )

  def updated(x: Int, y: Int, elem: T): Vector2D[T] =
    Vector2D(width, vector.updated(toIndex(x, y), elem))

object Vector2D:
  def apply[T](rows: Vector[Vector[T]]): Option[Vector2D[T]] =
    rows.headOption.fold(empty.some) { head =>
      Option.when(rows.forall(_.size == head.size))(Vector2D(head.size, rows.flatten))
    }

  def empty[T]: Vector2D[T] =
    Vector2D(0, Vector.empty)

  def single[T](elem: T): Vector2D[T] =
    Vector2D(1, Vector(elem))

  def fill[T](width: Int, height: Int)(elem: T): Vector2D[T] =
    Vector2D(width, Vector.fill(width * height)(elem))
