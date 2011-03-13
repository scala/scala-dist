package tutorial

class Complex(real: double, imaginary: double) {
  def re() = real
  def im() = imaginary
}

object ComplexNumbers {
  def main(args: Array[String]) {
    val c = new Complex(1.2, 3.4)
    println("imaginary part: " + c.im())
  }
}
