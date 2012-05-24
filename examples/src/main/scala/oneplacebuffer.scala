package examples

object onePlaceBuffer {

  import scala.actors.Actor._
  import scala.concurrent._

  class OnePlaceBuffer {
    private case class Put(x: Int)
    private case object Get
    private case object Stop

    private val m = actor {
      var buf: Option[Int] = None
      loop {
        react {
          case Put(x) if buf.isEmpty =>
            println("put "+x); 
            buf = Some(x); reply()
          case Get if !buf.isEmpty =>
            val x = buf.get
            println("get "+x)
            buf = None; reply(x)
          case Stop => exit()
        }
      }
    }

    def write(x: Int) { m !? Put(x) }

    def read(): Int = (m !? Get).asInstanceOf[Int]

    def finish() { m ! Stop }
  }

  def main(args: Array[String]) {
    val buf = new OnePlaceBuffer
    val random = new java.util.Random()
    val sinker = new SyncVar[Boolean]
    val MaxWait = 500L
    def isDone = sinker.isSet && sinker.get(MaxWait).getOrElse(false)
    def finish = sinker.put(true)
    def kill(delay: Long) = new java.util.Timer().schedule(
      new java.util.TimerTask {
        override def run() { finish }
      },
      delay) // in milliseconds
    def producer(n: Int) {
      if (isDone) { buf write -1; return }
      Thread.sleep(random nextInt 1000)
      buf write n
      producer(n + 1)
    }
    def consumer {
      Thread.sleep(random nextInt 1000)
      val n = buf.read()
      if (n < 0) return
      consumer
    }

    val maker = future { producer(0) }
    maker onComplete { e => buf.finish() }
    val taker = future { consumer }
    kill(10000)
  }
}
