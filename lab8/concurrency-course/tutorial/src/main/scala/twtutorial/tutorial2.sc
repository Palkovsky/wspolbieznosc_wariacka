object tutorial2 {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  ////////////////
  // 1. Classes //
  ////////////////

  // The following definition (single line!) introduces:
  // - new type Rational
  // - object constructor Rational(x: Int, y: Int)
  // - two fields: numer: Int, denom: Int
  {
    class Rational(val numer: Int, val denom: Int)

    val r1 = new Rational(1, 2)
    r1.numer
  }                                               //> res0: Int = 1

  // Let's add a few methods to this class:
  class Rational(val numer: Int, val denom: Int) {

    // another constructor can be added like this:
    def this(x: Int) = this(x, 1)

    // adds two Rational numbers and returns a new Rational which represents the sum
    def add(that: Rational) =
      new Rational(
        this.numer * that.denom + that.numer * this.denom,
        this.denom * that.denom)

    override def toString = numer + "/" + denom
  }

  val r1 = new Rational(2)                        //> r1  : tutorial2.Rational = 2/1
  val r2 = new Rational(3, 4)                     //> r2  : tutorial2.Rational = 3/4

  // all two-argument functions can be written in an infix notation
  // "r1.add(r2)" is equivalent to "r1 add r2"
  // in fact method "add" could be named "+" because it is a valid identifier!
  // -> please make this modification
  r1 add r2                                       //> res1: tutorial2.Rational = 11/4

  ////////////////////////////////////
  // 2. Abstract classes and traits //
  ////////////////////////////////////

  // When to use which -> read: http://www.artima.com/pins1ed/traits.html#12.7

  trait IntSet {
    def incl(x: Int): IntSet
    def contains(x: Int): Boolean
    def isEmpty: Boolean
  }

  class Empty extends IntSet {
    def contains(x: Int): Boolean = false
    def isEmpty: Boolean = true
    def incl(x: Int) = new NonEmpty(List(x))
  }

  class NonEmpty(val elements: List[Int]) extends IntSet {
    def this(x: Int) = this(List(x))
    def contains(x: Int): Boolean = elements contains x
    def isEmpty: Boolean = false
    def incl(x: Int) =
      if (contains(x))
        this
      else
        new NonEmpty((x :: elements).sorted)

    override def toString = elements mkString ","
  }

  val s1 = new NonEmpty(1)                        //> s1  : tutorial2.NonEmpty = 1
  val s2 = s1 incl 2                              //> s2  : tutorial2.IntSet = 1,2
  val s3 = s2 incl 1                              //> s3  : tutorial2.IntSet = 1,2

}