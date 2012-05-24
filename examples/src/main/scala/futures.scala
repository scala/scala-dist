package examples

import concurrent._
import java.util.concurrent.TimeUnit._

object futures {
  def someLengthyComputation = 1
  def anotherLengthyComputation = 2
  def f(x: Int) = x + x
  def g(x: Int) = x * x

  def main(args: Array[String]) {
    val d = util.Duration(1, SECONDS).toMillis
    val xf = future(someLengthyComputation)
    val yf = future(anotherLengthyComputation)
    val xr = new SyncVar[Int]
    val yr = new SyncVar[Int]
    xf onSuccess {
      case v => xr.put(v)
    }
    yf onSuccess {
      case v => yr.put(v)
    }
    def x = xr.get(d).get
    def y = yr.get(d).get
    val z = f(x) + g(y)
    println(z)
  }
}
