package tutorial

object TimerAnonymous {
  def oncePerSecond(callback: () => unit): unit =
    while (true) { callback(); Thread sleep 1000 }

  def main(args: Array[String]): unit =
    oncePerSecond(() =>
      Console.println("time flies like an arrow..."))
}

