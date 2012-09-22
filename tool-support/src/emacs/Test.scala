package foo

import bar

object Foo extends Bar with Zot

object Foo extends Bar(1,
                       2)
           with Zot(3)

object Foo extends Bar;
with Zot // should not be aligned with 'extends' since there is ';' above

object Foo extends Bar

with Zot // should not be aligned with 'extends' since there is empty line above

private class Foo 
        extends Bar
        with Zot

private class Foo { self =>
  line
}

private class Foo { 
  self =>
    line // KNOWN ISSUE, should not be indented
}

private class Foo { 
  self =>

  line
}

private class Foo(x: Int,
                  y: Int) // KNOWN ISSUE in font-lock mode

private[Foo] class Foo(x: Int, y: Int) extends Bar(x, y)
                                       with Zot


/* indenting */
{
  def foo(x: Int,
          y: Int)
  (z: Int) // KNOWN ISSUE: curry is not aligned nicely

  def x = 1
  def y = true
  def z = if (y)
            x
          else 
            0

  var z = if (y) 
            if (z > 0)
              3
            else
              z
            else // KNOWN ISSUE, should be aligned with first if, but we will not parse such things
              2

  val z = if (y) {
    2
  } else {
    if (false)
      println("foo")
    3
  }

  val foo = zot (y) {
    bar
  }

  val x = new Foo(1,
                  2,
                  3)
          with Bar

  val foo = zot map (x =>
    x.toString)

  val foo = zot map (x =>
    x.toString
                   ) // KNOWN ISSUE

  def zz = for (i <- 1 to 10)
           yield i

  def yy = for {
    i < 1 to 10
  } yield i

  def yy = for { i <- 1 to 10
                 j <- 2 to 3 }
           yield i * j

  def z = x match {
    case 1 =>
      foo
    bar // KNOWN ISSUE, should be indented
    case a =>
      bar
  }

  do {
    something
  } while (true)

  while (true) {
    something
  }

  while (true)
  something // KNWN ISSUE, should be indented, but we don't know if the while is end of do..while or start of plain while

  def z = try {
    foo
  } catch {
    case e =>
      oneline
    twoline // KNOWN ISSUE, should be indented
  }

  def z = foo
    .bar
    .zot

  def z = foo {
  }
  .bar
  .bar

}

/* font lock */
private/* */class/* */Foo/* */[+T]/* */(i: X,
                                        j: Y) // KNOWN ISSUE: does not highlight when typed

{
  def x = 1

  def/* */x/* */=/* */1 // KNOWN ISSUE: x is with wrong face

  def foo(x: String, //
          y: Int/* */, // KNOWN ISSUE: Int should be highlighted
          z: Boolean)
  (x: Int) // KNOWN ISSUE(S): '(' is highted, curry is not highlighted when typed

  def foo(@annotation // KNOWN ISSUE: annotations are in parameter name font face
          x: String)

  val x = new Foo(1,
                  2,
                  3)
          with Bar // KNOWN ISSUE: bar is in wrong font-face (should be same as Foo)

}
