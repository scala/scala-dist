package tutorial

trait Ord {
  def < (that: Any): boolean
  def <=(that: Any): boolean = (this < that) || (this == that)
  def > (that: Any): boolean = !(this <= that)
  def >=(that: Any): boolean = !(this < that)
}

class Date(y: int, m: int, d: int) extends Ord {
  def year = y
  def month = m
  def day = d

  override def toString(): String =
    year + "" + month + "" + day

  override def equals(that: Any): boolean =
    that.isInstanceOf[Date] && {
      val o = that.asInstanceOf[Date]
      o.day == day && o.month == month && o.year == year
    }

  def <(that: Any): boolean = {
    if (!that.isInstanceOf[Date])
      error("cannot compare " + that + " and a Date")
    val o = that.asInstanceOf[Date]
    (year < o.year) ||
    (year == o.year && (month < o.month ||
    (month == o.month && day < o.day)))
  }
}

object Dates {
  def main(args: Array[String]) {
    val d1 = new Date(1997, 1, 1)
    val d2 = new Date(1996, 2, 1)
    println(d1 < d2)
  }
}
