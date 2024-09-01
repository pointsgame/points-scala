package points.field

enum Player derives CanEqual:
  case Red, Black

  def opponent: Player =
    this match
      case Red => Black
      case Black => Red

object Player:
  def apply(boolean: Boolean): Player =
    if boolean then Red
    else Black
