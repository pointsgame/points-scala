package points.field

final case class Pos(x: Int, y: Int) derives CanEqual:
  inline def n: Pos =
    Pos(x, y + 1)
  inline def s: Pos =
    Pos(x, y - 1)
  inline def w: Pos =
    Pos(x - 1, y)
  inline def e: Pos =
    Pos(x + 1, y)

  inline def nw: Pos =
    Pos(x - 1, y + 1)
  inline def ne: Pos =
    Pos(x + 1, y + 1)
  inline def sw: Pos =
    Pos(x - 1, y - 1)
  inline def se: Pos =
    Pos(x + 1, y - 1)

  inline def dx(pos: Pos): Int =
    x - pos.x
  inline def dy(pos: Pos): Int =
    y - pos.y

  inline def tuple: (Int, Int) =
    (x, y)

  inline def skewProduct(other: Pos): Int =
    x * other.y - other.x * y
