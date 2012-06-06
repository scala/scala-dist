package examples

object boundedBuffer {
  import concurrent.{ future, Future }

  class BoundedBuffer[A](N: Int)(implicit m: ArrayTag[A]) {
    var in, out, n = 0
    val elems = new Array[A](N)

    def await(cond: => Boolean) = while (!cond) { wait() }

    def put(x: A) = synchronized {
      await (n < N)
      elems(in) = x; in = (in + 1) % N; n += 1
      if (n == 1) notifyAll()
    }

    def get: A = synchronized {
      await (n != 0)
      val x = elems(out); out = (out + 1) % N ; n -= 1
      if (n == N - 1) notifyAll()
      x
    }
  }

  def main(args: Array[String]) {
    val buf = new BoundedBuffer[String](10)
    val Halt = "halt"
    val maker = future {
      var cnt = 0
      def produceString = { cnt += 1; cnt.toString() }
      while (cnt < 10) {
        buf.put(produceString)
      }
      buf.put(Halt)
    }
    val taker = future {
      import collection.mutable.ListBuffer
      val res = ListBuffer[String]()
      def consumeString(s: String) = res += s
      var done = false
      while (!done) {
        val s = buf.get
        if (s == Halt) done = true
        else consumeString(s)
      }
      res.toList
    }
    taker onSuccess {
      case res: List[_] => res foreach println
    }
  }
}
