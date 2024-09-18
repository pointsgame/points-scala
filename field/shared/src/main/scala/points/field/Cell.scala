package points.field

enum Cell derives CanEqual:
  case Empty
  case Point(player: Player)
  case Base(player: Player, enemy: Boolean)
  case EmptyBase(player: Player)

  def isFree: Boolean = this match
    case Empty => true
    case EmptyBase(_) => true
    case _ => false

  def isEmptyBase(player: Player): Boolean = this match
    case EmptyBase(p) => p == player
    case _ => false

  def isOwner(player: Player): Boolean = this match
    case Point(p) => p == player
    case Base(p, _) => p == player
    case _ => false

  def isPlayersPoint(player: Player): Boolean = this match
    case Point(p) => p == player
    case _ => false

  def isCapturedPoint(player: Player): Boolean = this match
    case Base(p, enemy) => p == player.opponent && enemy
    case _ => false

  def capture(player: Player): Cell = this match
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
