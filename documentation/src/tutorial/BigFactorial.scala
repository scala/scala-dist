package tutorial

object BigFactorial {
  import java.math.BigInteger, BigInteger._

  def fact(x: BigInteger): BigInteger =
    if (x == ZERO) ONE
    else x multiply fact(x subtract ONE)

  def main(args: Array[String]) {
    val x = new BigInteger("100")
    println("fact(100) = " + fact(x))
  }
}
