package points.field

import cats.Comparison
import cats.data.NonEmptyList
import cats.implicits.*

import scala.annotation.tailrec

final case class Field private (
  cells: Vector2D[Cell],
  scoreRed: Int,
  scoreBlack: Int,
  moves: List[(Pos, Player)],
  lastSurroundPlayer: Player,
  lastSurroundChains: List[NonEmptyList[Pos]],
) derives CanEqual:
  given nelCanEqual[A, B](using CanEqual[A, B]): CanEqual[NonEmptyList[A], NonEmptyList[B]] =
    CanEqual.canEqualAny

  given comparisonCanEqual: CanEqual[Comparison, Comparison] =
    CanEqual.canEqualAny

  inline def width: Int = cells.width
  inline def height: Int = cells.height

  inline def lastPlayer: Option[Player] =
    moves.headOption.map(_._2)

  inline def apply(pos: Pos): Cell =
    cells(pos.x, pos.y)

  inline def get(pos: Pos): Option[Cell] =
    cells.get(pos.x, pos.y)

  inline def isInside(pos: Pos): Boolean =
    pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height

  inline def isPuttingAllowed(pos: Pos): Boolean =
    get(pos).exists(_.isFree)

  inline def isOwner(pos: Pos, player: Player): Boolean =
    get(pos).exists(_.isOwner(player))

  inline def isPlayersPoint(pos: Pos, player: Player): Boolean =
    get(pos).exists(_.isPlayersPoint(player))

  inline def isCapturedPoint(pos: Pos, player: Player): Boolean =
    get(pos).exists(_.isCapturedPoint(player))

  private def getFirstNextPos(centerPos: Pos, pos: Pos): Pos = pos.dx(centerPos) -> pos.dy(centerPos) match
    case (-1, -1) => centerPos.se
    case (0, -1) => centerPos.ne
    case (1, -1) => centerPos.ne
    case (-1, 0) => centerPos.se
    case (1, 0) => centerPos.nw
    case (-1, 1) => centerPos.sw
    case (0, 1) => centerPos.sw
    case (1, 1) => centerPos.nw
    case _ => throw new IllegalArgumentException(s"not adjacent points: $centerPos and $pos")

  private def getNextPos(centerPos: Pos, pos: Pos): Pos = (pos.dx(centerPos), pos.dy(centerPos)) match
    case (-1, -1) => pos.e
    case (0, -1) => pos.e
    case (1, -1) => pos.n
    case (-1, 0) => pos.s
    case (1, 0) => pos.n
    case (-1, 1) => pos.s
    case (0, 1) => pos.w
    case (1, 1) => pos.w
    case _ => throw new IllegalArgumentException(s"not adjacent points: $centerPos and $pos")

  private def square(chain: NonEmptyList[Pos]): Int =
    @tailrec
    def _square(l: NonEmptyList[Pos], acc: Int): Int = l match
      case NonEmptyList(h, Nil) => acc + h.skewProduct(chain.head)
      case NonEmptyList(h1, h2 :: t) => _square(NonEmptyList(h2, t), acc + h1.skewProduct(h2))
    _square(chain, 0)

  private def buildChain(startPos: Pos, nextPos: Pos, player: Player): Option[NonEmptyList[Pos]] =
    @tailrec
    def getNextPlayerPos(centerPos: Pos, pos: Pos): Pos =
      if pos == startPos || isOwner(pos, player) then pos
      else getNextPlayerPos(centerPos, getNextPos(centerPos, pos))

    @tailrec
    def getChain(start: Pos, list: NonEmptyList[Pos]): NonEmptyList[Pos] =
      val h = list.head
      val nextPos = getNextPlayerPos(h, getFirstNextPos(h, start))
      if nextPos == startPos then list
      else getChain(h, NonEmptyList.fromList(list.toList.dropWhile(_ != nextPos)).getOrElse(nextPos :: list))

    val chain = getChain(startPos, NonEmptyList.of(nextPos, startPos))
    if square(chain) > 0 then chain.some
    else none

  private def getInputPoints(pos: Pos, player: Player): List[(Pos, Pos)] =
    val list1 =
      if !isOwner(pos.w, player) then
        if isOwner(pos.sw, player) then (pos.sw, pos.w) :: Nil
        else if isOwner(pos.s, player) then (pos.s, pos.w) :: Nil
        else Nil
      else Nil
    val list2 =
      if !isOwner(pos.n, player) then
        if isOwner(pos.nw, player) then (pos.nw, pos.n) :: list1
        else if isOwner(pos.w, player) then (pos.w, pos.n) :: list1
        else list1
      else list1
    val list3 =
      if !isOwner(pos.e, player) then
        if isOwner(pos.ne, player) then (pos.ne, pos.e) :: list2
        else if isOwner(pos.n, player) then (pos.n, pos.e) :: list2
        else list2
      else list2
    val list4 =
      if !isOwner(pos.s, player) then
        if isOwner(pos.se, player) then (pos.se, pos.s) :: list3
        else if isOwner(pos.e, player) then (pos.e, pos.s) :: list3
        else list3
      else list3
    list4

  private def isPosInsideRing(pos: Pos, ring: NonEmptyList[Pos]): Boolean =
    def removeNearSame(list: List[Int]): List[Int] =
      list.foldRight(List.empty[Int])((a, acc) => if acc.headOption.contains(a) then acc else a :: acc)
    NonEmptyList
      .fromList(removeNearSame(ring.filter(_.x <= pos.x).map(_.y)))
      .fold(false): coords =>
        val _coords =
          if coords.last == pos.y then
            coords ++ (if coords.head == pos.y then coords.tail else coords.toList).headOption.toList
          else if coords.head == pos.y then coords.last :: coords
          else coords
        _coords.toList.zip(_coords.tail).zip(_coords.tail.drop(1)).count { case ((a, b), c) =>
          b == pos.y && ((a < b && c > b) || (a > b && c < b))
        } % 2 == 1

  def wave(startPos: Pos, f: Pos => Boolean): Set[Pos] =
    def neighborhood(pos: Pos): List[Pos] =
      List(pos.n, pos.s, pos.w, pos.e)
    def nextFront(passed: Set[Pos], front: Set[Pos]): Set[Pos] =
      front.flatMap(neighborhood).filter(isInside).diff(passed).filter(f)
    @tailrec
    def _wave(passed: Set[Pos], front: Set[Pos]): Set[Pos] =
      if front.isEmpty then passed
      else _wave(passed.union(front), nextFront(passed, front))
    _wave(Set.empty, Set(startPos))

  private def getInsideRing(startPos: Pos, ring: NonEmptyList[Pos]): Set[Pos] =
    val ringSet = ring.toList.toSet
    wave(startPos, !ringSet.contains(_))

  private def getEmptyBase(startPos: Pos, player: Player): (NonEmptyList[Pos], Set[Pos]) =
    @tailrec
    def getEmptyBaseChain(pos: Pos): NonEmptyList[Pos] =
      if !isOwner(pos, player) then getEmptyBaseChain(pos.w)
      else
        val inputPoints = getInputPoints(pos, player)
        val chains = inputPoints.flatMap { case (chainPos, _) => buildChain(pos, chainPos, player) }
        chains.find(isPosInsideRing(startPos, _)) match
          case Some(result) => result
          case None => getEmptyBaseChain(pos.w)
    val emptyBaseChain = getEmptyBaseChain(startPos.w)
    (emptyBaseChain, getInsideRing(startPos, emptyBaseChain).filter(apply(_).isFree))

  def putPoint(pos: Pos, player: Player): Option[Field] =
    if !isPuttingAllowed(pos) then none
    else
      val enemy = player.opponent
      val value = apply(pos)
      val newMoves = (pos, player) :: moves
      if value.isEmptyBase(player) then
        copy(
          cells = cells.updated(pos.x, pos.y, Cell.Point(player)),
          moves = newMoves,
          lastSurroundPlayer = player,
          lastSurroundChains = List.empty,
        ).some
      else
        val fieldWithCaptures = getInputPoints(pos, player)
          .flatMap { case (chainPos, capturedPos) =>
            buildChain(pos, chainPos, player).map(_ -> capturedPos)
          }
          .sortBy(_._1.size)
          .foldLeft {
            this.copy(
              lastSurroundPlayer = player,
              lastSurroundChains = List.empty,
            )
          } { case (field, (chain, capturedPos)) =>
            val captured = field.getInsideRing(capturedPos, chain)
            val capturedCount = captured.count(field.isPlayersPoint(_, enemy))
            val freedCount = captured.count(field.isCapturedPoint(_, player))
            if capturedCount > 0 then
              val newScoreRed =
                if player == Player.Red then field.scoreRed + capturedCount else field.scoreRed - freedCount
              val newScoreBlack =
                if player == Player.Black then field.scoreBlack + capturedCount else field.scoreBlack - freedCount
              field.copy(
                cells = captured.foldLeft(field.cells)((acc, p) => acc.updated(p.x, p.y, field(p).capture(player))),
                scoreRed = newScoreRed,
                scoreBlack = newScoreBlack,
                lastSurroundChains = chain :: field.lastSurroundChains,
              )
            else
              field.copy(cells =
                captured
                  .filter(p => field(p) == Cell.Empty)
                  .foldLeft(field.cells)((acc, p) => acc.updated(p.x, p.y, Cell.EmptyBase(player))),
              )
          }
        if value.isEmptyBase(enemy) then
          if fieldWithCaptures.lastSurroundChains.nonEmpty then
            val enemyEmptyBase = fieldWithCaptures.wave(pos, pos => fieldWithCaptures(pos).isEmptyBase(enemy))
            fieldWithCaptures
              .copy(
                cells = enemyEmptyBase
                  .foldLeft(fieldWithCaptures.cells)((acc, p) => acc.updated(p.x, p.y, Cell.Empty))
                  .updated(pos.x, pos.y, Cell.Point(player)),
                moves = newMoves,
              )
              .some
          else
            val (enemyEmptyBaseChain, enemyEmptyBase) = getEmptyBase(pos, enemy)
            copy(
              cells = enemyEmptyBase.foldLeft(cells)((acc, p) => acc.updated(p.x, p.y, Cell.Base(enemy, p == pos))),
              scoreRed = if player == Player.Red then scoreRed else scoreRed + 1,
              scoreBlack = if player == Player.Black then scoreBlack else scoreBlack + 1,
              moves = newMoves,
              lastSurroundPlayer = enemy,
              lastSurroundChains = List(enemyEmptyBaseChain),
            ).some
        else
          fieldWithCaptures
            .copy(
              cells = fieldWithCaptures.cells.updated(pos.x, pos.y, Cell.Point(player)),
              moves = newMoves,
            )
            .some

  inline def nextPlayer: Player =
    lastPlayer.map(_.opponent).getOrElse(Player.Red)

  inline def putPoint(pos: Pos): Option[Field] =
    putPoint(pos, nextPlayer)

  inline def winner: Option[Player] =
    scoreRed.comparison(scoreBlack) match
      case Comparison.GreaterThan => Player.Red.some
      case Comparison.LessThan => Player.Black.some
      case Comparison.EqualTo => none

  override def equals(o: Any): Boolean = o match
    case that: Field =>
      that.width == width &&
      that.height == height &&
      that.moves == moves
    case _ => false

  override def hashCode: Int =
    val n = 41
    val h1 = 3 * width
    val h2 = h1 * n + height
    val h3 = h2 * n + moves.hashCode
    h3

object Field:
  def apply(width: Int, height: Int): Field =
    new Field(Vector2D.fill(width, height)(Cell.Empty), 0, 0, Nil, Player.Red, List.empty)
