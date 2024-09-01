package points.field

trait Colored {
  def player: Player
}

final case class ColoredPos(pos: Pos, player: Player) extends Colored derives CanEqual

final case class ColoredChain(chain: List[Pos], player: Player) extends Colored derives CanEqual
