val root = new Any { rootThis =>
  trait Unit extends Any {}
  val unit: Any => rootThis.Unit
  trait Boolean extends Any {
    val ifNat: (rootThis.Unit => rootThis.Nat) => (rootThis.Unit => rootThis.Nat) => rootThis.Nat
  }
  val false: rootThis.Unit => rootThis.Boolean
  val true: rootThis.Unit => rootThis.Boolean
  trait Nat extends Any {
    val isZero: rootThis.Unit => rootThis.Boolean
    val pred: rootThis.Unit => rootThis.Nat
    val succ: rootThis.Unit => rootThis.Nat
    val add: rootThis.Nat => rootThis.Nat
  }  
  val zero: rootThis.Unit => rootThis.Nat
  val successor: rootThis.Nat => rootThis.Nat
  val add2: rootThis.Nat => rootThis.Nat => rootThis.Nat
  val error: rootThis.Unit => Bot
} {
  val unit = (x: Any) => val u = new root.Unit; u
  val false = (x: root.Unit) => {
    val ff = new root.Boolean {
      val ifNat = (t: root.Unit => root.Nat) => (e: root.Unit => root.Nat) => e(root.unit)
    }
    ff
  }
  val true = (x: root.Unit) => {
    val tt = new root.Boolean {
      val ifNat = (t: root.Unit => root.Nat) => (e: root.Unit => root.Nat) => t(root.unit)
    }
    tt
  }
  val zero = (x: root.Unit) => {
    val zz = new root.Nat { 
      val isZero = (x: root.Unit) => root.false(root.unit)
      val succ = (x: root.Unit) => root.successor(zz)
      val pred = (x: root.Unit) => error(root.unit)
      val add = (other: root.Nat) => add2(other, zz)
    }
    zz
  }
  val successor = (n: root.Nat) => {
    val ss = new root.Nat {
      val isZero = (x: root.Unit) => root.true(root.unit)
      val succ = (x: root.Unit) => root.successor(ss)
      val pred = (x: root.Unit) => n
      val add = (other: root.Nat) => add2(other, ss)
    }
    ss
  }
  val add2 = (n1: root.Nat) => (n2: root.Nat) =>
    n1.isZero(root.unit).ifNat
     ((x: root.Unit) => n2)
     ((x: root.Unit) => root.add2(n1.pred(root.unit))(n2.succ(root.unit)))
  val error = (x: root.Unit) => error(x)
}
val lists = new Any { listsThis => 
  trait List { thisList => 
    type Elem
    type ListOfElem = List { type Elem = thisList.Elem }
    val isEmpty: root.Unit => root.Boolean
    val head: root.Unit => root.Boolean
    val tail: root.Unit => thisList.ListOfElem
  }
  trait Nil extends listsThis.List {
    
   
        









          
    }


  }
 
    
  trait Boolean extends Any {
    def if: Boolean => Any => 

  trait Nat extends Any { this0 =>
    val isZero: Boolean
    val pred: 

trait Nat extends Any { this0 |
  def isZero(): Boolean
  def pred(): Nat
  trait Succ extends Nat { this1 |
    def isZero(): Boolean = false
    def pred(): Nat = this0 
  }
  def succ(): Nat = ( val result = new this0.Succ; result )
  def add(other: Nat): Nat = (
    if (this0.isZero()) other else this0.pred().add(other.succ())
  )
  def subtract(other: Nat): Nat = (
    if (other.isZero()) this0 else this0.pred().subtract(other.pred())
  )
}

val zero = new Nat { this0 |
  def isZero(): Boolean = true
  def pred(): Nat = error("zero.pred")
}
