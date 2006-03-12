package tutorial

class Reference[a] {
  private var contents: a = _
  def set(value: a): Unit = { contents = value }
  def get: a = contents
}

object IntegerReference {
  def main(args: Array[String]): Unit = {
    val cell = new Reference[Int]
    cell.set(13)
    Console.println("Reference contains the half of " + (cell.get * 2))
  }
}

