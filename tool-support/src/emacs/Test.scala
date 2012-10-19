def collect[L,R](futures: Seq[MappableFuture[Either[L,R]]]) =
(futures.view map (_.apply())).foldLeft((List.empty[L], List.empty[R])) {
  case ((ls, rs), Left(l)) => (l :: ls, rs);
  case ((ls, rs), Right(r)) => (ls, r :: rs)
}

{
  def ping() = {
    execute(RestClient.asyncHttpClient.prepareGet(pingUrl)) map {
      case None => (2, pingUrl, "Could not connect to service")
      case Some(response) =>
        if (response.getResponseBody() == "OK") {
          (0, pingUrl, "OK")
        } else {
          (1, pingUrl, "Response was not 'OK', was " +
           response.getResponseBody().take(60))
        }
    }
  }
}


package foo

import bar

object Foo extends Bar with Zot

object Foo extends Bar(  1,
                         2)( 3,
                             4)
           with Zot(3) { foo:Bar => 
  
  def foo(x: String,
          y: String) = /*
          */
    x + y

  def ping() =
    execute(RestClient.asyncHttpClient.prepareGet(pingUrl)) map {
      case None(foo) =>
        if (foo) {
          bar
        } else {
          fobar
        }
    }
 
  foo(x,
      y) ( asd => (  _ map { d => 
        and some;
        more 
      }))
  
  val x = "foo"; val f = 
    "zot"


  val z = x match {
    case f: Seq[Int) => 
      f map ( i =>
        i + 1
      )
  }

  def foozot = 
    x match { 
      case x: String  =>
        foo
        zot
    }
  
  def f: String = 
  {
    1
  }
  
}

class Foo[E](path: String,
             valueClass: Class) {
  asd
}

def f(a: Foo => Bar, 
      b: Zot)
     (c: Kala, d: Kisa): (Foo => (Bar, Zot)
                          Option[x] forSome { 
       type x <: Kissa 
     }) = {
  magic!
}

def f(g: => (String, Int),
      x: String) =
  g(x)

{
  private class SparkInput[T[T,C]] (path: String,
                                    inputFormatClass: Class, 
                                    valueClass: Class[V]) /* */ (x: String,
                                                                 y: String) /* */
                                                                (z: String)
          extends Zot[E](x,y)(z) {
    
    def x[Foo
          with Bar
          forSome {
      val Z: X
    }]: Bar 
    with Zot // KNOWN ISSUE: still broken
  }
}

object Foo extends Bar;
with Zot // should not be aligned with 'extends' since there is ';' above

object Foo extends Bar

with Zot // should not be aligned with 'extends' since there is empty line above

private class Foo 
        extends Bar
        with Zot 
{
  private class Foo
          extends Bar { self /* */ : /* */ Zot /* */ [A, /* */ B[C, D]] /* */ =>
    line1
    line2
  }
}

private class Foo {
  self: Zot =>
  line
  
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
                                       with Zot { self: /* */ Option[String,
                                                                     And,
                                                                     Some[x] forSome {
                                         type x <% String
                                       }] =>
  line1;
  
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
          with Bar(2)(3,
                      4
                      5) {
    asd
  }
  
  val foo = zot map (x: String => {
    x.toString 
  })

  val foo = zot map (x: String => { some;
                                    more; }) // KNOWN ISSUE: above is not detected as lambda since => does not end the line
  
  val foo = zot map (x: Option[String] =>
    x.toString
  )
  
  foo map (a: String => println(a);
           a.length) // KNOWN ISSUE: above is not detected as lambda since => does not end the line

  
  def zz = for (i <- 1 to 10)
           yield i
  
  def yy = for {
    i < 1 to 10
  } yield i
  
  def yy = for { i <- 1 to 10
                 j <- 2 to 3 }
           yield i * j
  
  def z = x match {
      
    case l: String =>
      foo
      bar
    /* foo bar*/
    case a => {
      bar
      goo
    }
  }
  
  val b = doSome(x,
                 y)
                (foo, zot) {
    case x =>
      foo
      bar
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
    g
  }
  
  {
    foo(x,
        y) { z =>
      println(z)
      z + 1
    }
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

