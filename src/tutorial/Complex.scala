package tutorial

class Complex(real: double, imaginary: double) {
  def re() = real
  def im() = imaginary
}

object Complex {
  def main(args: Array[String]): Unit = {
    val c = new Complex(1.2, 3.4)
    Console.println("imaginary part: " + c.im())
  }
}

