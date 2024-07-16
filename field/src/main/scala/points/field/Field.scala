package points.field

import cats.data.NonEmptyList
import cats.implicits.*

import scala.annotation.tailrec

final class Field private (
  val cells: Vector2D[Cell],
  val scoreRed: Int,
  val scoreBlack: Int,
  val moves: List[ColoredPos],
  val lastSurroundChain: Option[ColoredChain],
) derives CanEqual:
  def width: Int = cells.width
  def height: Int = cells.height

  def lastPlayer: Option[Player] =
    moves.headOption.map(_.player)

  def apply(pos: Pos): Cell =
    cells(pos.x, pos.y)

  def get(pos: Pos): Option[Cell] =
    cells.get(pos.x, pos.y)

  def isInside(pos: Pos): Boolean =
    pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height

  def isPuttingAllowed(pos: Pos): Boolean =
    get(pos).exists(_.isFree)

  def isOwner(pos: Pos, player: Player): Boolean =
    get(pos).exists(_.isOwner(player))

  def isPlayersPoint(pos: Pos, player: Player): Boolean =
    get(pos).exists(_.isPlayersPoint(player))

  def isCapturedPoint(pos: Pos, player: Player): Boolean =
    get(pos).exists(_.isCapturedPoint(player))

  private def getFirstNextPos(centerPos: Pos, pos: Pos): Pos = pos.dx(centerPos) -> pos.dy(centerPos) match
    case (-1, -1) => centerPos.se
    case (0, -1) => centerPos.ne
    case (1, -1) => centerPos.ne
    case (-1, 0) => centerPos.se
    case (0, 0) => centerPos.se
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
    case (0, 0) => pos.s
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
      if pos == startPos then pos
      else if isOwner(pos, player) then pos
      else getNextPlayerPos(centerPos, getNextPos(centerPos, pos))

    @tailrec
    def getChain(start: Pos, list: NonEmptyList[Pos]): NonEmptyList[Pos] =
      val h = list.head
      val nextPos = getNextPlayerPos(h, getFirstNextPos(h, start))
      if nextPos == startPos then list
      else getChain(h, NonEmptyList.fromList(list.toList.dropWhile(_ != nextPos)).getOrElse(nextPos :: list))

    val chain = getChain(startPos, NonEmptyList.of(nextPos, startPos))
    if chain.length > 2 && square(chain) > 0 then chain.some
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
      list.foldRight(List(list.last))((a, acc) => if acc.head == a then acc else a :: acc)
    val _ring = removeNearSame(ring.filter(_.x <= pos.x).map(_.y))
    val __ring =
      if _ring.last == pos.y then _ring :+ (if _ring.head == pos.y then _ring.tail else _ring).head
      else if _ring.head == pos.y then _ring.last :: _ring
      else _ring
    __ring.zip(__ring.tail).zip(__ring.tail.tail).count { case ((a, b), c) =>
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

  private def capture(posValue: Cell, player: Player): Cell = posValue match
    case Cell.Empty =>
      Cell.Base(player, false)
    case Cell.Point(p) =>
      if p == player then Cell.Point(p)
      else Cell.Base(player, true)
    case Cell.Base(p, enemy) =>
      if p == player then Cell.Base(p, enemy)
      else if enemy then Cell.Point(player)
      else Cell.Base(player, false)
    case Cell.EmptyBase(_) =>
      Cell.Base(player, false)

  private def mergeCaptureChains(pos: Pos, chains: List[NonEmptyList[Pos]]): List[Pos] =
    @tailrec
    def _mergeCaptureChains(l: List[NonEmptyList[Pos]]): List[Pos] =
      val first = l.head
      val last = l.last
      if first.head != last.toList(last.size - 2) then
        l.map(_.toList)
          .flatten
          .foldRight(List.empty[Pos])((p, acc) =>
            if p != pos && acc.contains(p) then acc.dropWhile(_ != p) else p :: acc,
          )
      else _mergeCaptureChains(l.tail :+ first)
    if chains.size < 2 then chains.map(_.toList).flatten
    else _mergeCaptureChains(chains)

  def putPoint(pos: Pos, player: Player): Option[Field] =
    if !isPuttingAllowed(pos) then none
    else
      val enemy = player.opponent
      val value = apply(pos)
      if value.isEmptyBase(player) then
        new Field(
          cells.updated(pos.x, pos.y, Cell.Point(player)),
          scoreRed,
          scoreBlack,
          ColoredPos(pos, player) :: moves,
          none,
        ).some
      else
        val captures = getInputPoints(pos, player).flatMap { case (chainPos, capturedPos) =>
          for
            chain <- buildChain(pos, chainPos, player)
            captured = getInsideRing(capturedPos, chain)
            capturedCount = captured.count(isPlayersPoint(_, enemy))
            freedCount = captured.count(isCapturedPoint(_, player))
          yield (chain, captured, capturedCount, freedCount)
        }
        val (realCaptures, emptyCaptures) = captures.partition(_._3 != 0)
        val capturedCount = realCaptures.map(_._3).sum
        val freedCount = realCaptures.map(_._4).sum
        val realCaptured = realCaptures.flatMap(_._2)
        val captureChain = mergeCaptureChains(pos, realCaptures.map(_._1))
        if value.isEmptyBase(enemy) then
          val (enemyEmptyBaseChain, enemyEmptyBase) = getEmptyBase(pos, enemy)
          if captures.nonEmpty then
            val newScoreRed = if player == Player.Red then scoreRed + capturedCount else scoreRed - freedCount
            val newScoreBlack = if player == Player.Black then scoreBlack + capturedCount else scoreBlack - freedCount
            val updatedCells1 = enemyEmptyBase.foldLeft(cells)((acc, p) => acc.updated(p.x, p.y, Cell.Empty))
            val updatedCells2 = updatedCells1.updated(pos.x, pos.y, Cell.Point(player))
            val updatedCells3 =
              realCaptured.foldLeft(updatedCells2)((acc, p) => acc.updated(p.x, p.y, capture(apply(p), player)))
            new Field(
              updatedCells3,
              newScoreRed,
              newScoreBlack,
              ColoredPos(pos, player) :: moves,
              ColoredChain(captureChain, player).some,
            ).some
          else
            val newScoreRed = if player == Player.Red then scoreRed else scoreRed + 1
            val newScoreBlack = if player == Player.Black then scoreBlack else scoreBlack + 1
            val updatedCells =
              enemyEmptyBase.foldLeft(cells)((acc, p) => acc.updated(p.x, p.y, Cell.Base(enemy, p == pos)))
            new Field(
              updatedCells,
              newScoreRed,
              newScoreBlack,
              ColoredPos(pos, player) :: moves,
              ColoredChain(enemyEmptyBaseChain.toList, enemy).some,
            ).some
        else
          val newEmptyBase = emptyCaptures.flatMap(_._2).filter(p => cells(p.x, p.y) == Cell.Empty)
          val newScoreRed = if player == Player.Red then scoreRed + capturedCount else scoreRed - freedCount
          val newScoreBlack = if player == Player.Black then scoreBlack + capturedCount else scoreBlack - freedCount
          val updatedCells1 = cells.updated(pos.x, pos.y, Cell.Point(player))
          val updatedCells2 =
            newEmptyBase.foldLeft(updatedCells1)((acc, p) => acc.updated(p.x, p.y, Cell.EmptyBase(player)))
          val updatedCells3 =
            realCaptured.foldLeft(updatedCells2)((acc, p) => acc.updated(p.x, p.y, capture(apply(p), player)))
          new Field(
            updatedCells3,
            newScoreRed,
            newScoreBlack,
            ColoredPos(pos, player) :: moves,
            if captureChain.isEmpty then none else ColoredChain(captureChain, player).some,
          ).some

  def putPoint(pos: Pos): Option[Field] =
    putPoint(pos, lastPlayer.map(_.opponent).getOrElse(Player.Red))

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
    new Field(Vector2D.fill(width, height)(Cell.Empty), 0, 0, Nil, none)
