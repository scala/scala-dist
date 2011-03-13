package tutorial

object TimerAnonymous {
  def oncePerSecond(callback: () => unit) {
    while (true) { callback(); Thread sleep 1000 }
  }
  def main(args: Array[String]) {
    oncePerSecond(() =>
      println("time flies like an arrow..."))
  }
}
