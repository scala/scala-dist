package foo

import bar

object Foo extends Bar with Zot

object Foo extends Bar(  1,
                         2)( 3,
                             4)
           with Zot(3) { foo =>
             foo // KNOWN ISSUE, should not be indented this far
  bar
}

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

  case class Cell() 
  
  def f(): = {}

}

private class Foo { 
  
  case/* */
  class Cell() 
  
  def f(): = {}
  
}

private class Foo(x: Int,
                  y: Int,
                  z: Int) // KNOWN ISSUE in font-lock mode

private[Foo] class Foo(x: Int, y: Int) extends Bar(x, y)
                                       with Zot {
  
  case class Cell() 
  
  def f(): = {}
  
}


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
  
  val x = new Foo( /* */ 1,
                   2,
                   3)
          with Bar
  
  val foo = zot map (x =>
    x.toString)
  
  val foo = zot map (x =>
    x.toString
  ) // FIXED
  
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
      bar 
    /* foo bar*/
    case a => {
      bar
      goo
    }
  }
  
  do {
    something
  } while (true)
  
  while (true) {
    something
  }
  
  while (true)
  something // KNOWN ISSUE, should be indented, but we don't know if the while is end of do..while or start of plain while
  
  def z = try {
    foo
  } catch {
    case e =>
      abd
      oneline
      twoline 
  }
  
  // Scamacs works here
  def z = try {
    foo
  } catch {
    case e =>
      oneline
      twoline 
  }

  val zz = xx map {
    case (i, j) => 
      doSomething
      i+j
    case (j, i) =>
      doSomethingElse
      yes!
  }
  
  def z = foo
    .bar
    .zot
  
  // Scamacs always does below and never the above (no configuration)
  // Heikki, please confirm I haven't broken your dot alignment code. RPR
  // I actually prefer the below behavior.  
  // Since the Heikki added a custom param to support scamacs behavior, which I no longer like,
  // should we remove the param and go with below as fixed behavior.
  // I'm ok with removing the param.
  def z = foo {
    g.dothis
      .bar
      .bar
      .dothat
  }
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
