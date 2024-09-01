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
