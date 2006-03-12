package tutorial

object Timer {
  def oncePerSecond(callback: () => unit): unit =
    while (true) { callback(); Thread sleep 1000 }

  def timeFlies(): unit =
    Console.println("time flies like an arrow...")

  def main(args: Array[String]): unit =
    oncePerSecond(timeFlies)
}

