  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  //////////////////////////////////
  // 1. Evaluation of expressions //
  //////////////////////////////////

  // a. Evaluation "by name" and "by value"
  /////////////////////////////////////////

  // def vs. val
  def loop: Boolean = loop                        //> loop: => Boolean
  def x1 = loop                                   //> x1: => Boolean
  //val x2 = loop   // uncomment and try!

  // both parameters are evaluated "by value"
  def sqr(x: Double, y: Boolean) = x * x          //> sqr: (x: Double, y: Boolean)Double

  // "=>" means that the parameter will be evaluated "by name" (when referenced)
  def sqr2(x: Double, y: => Boolean) = x * x      //> sqr2: (x: Double, y: => Boolean)Double

  //sqr(2, loop)    // uncomment and try!
  sqr2(2, loop)                                   //> res0: Double = 4.0

  // b. if expression
  //////////////////////

  // in Scala, "if" is an expression (has value)
  def abs(x: Double) = if (x < 0) -x else x       //> abs: (x: Double)Double
  abs(-2)                                         //> res1: Double = 2.0
  abs(2)                                          //> res2: Double = 2.0

  // c. Blocks: { .... }
  /////////////////////////

  // Blocks may contain a sequence of definitions or expressions.
  // - Definitions are only visible inside the block
  // - Last element of the block must be an expression whose value is also the value of the block.

  // Example: tail-recursive factorial

  def fact(n: Int): Int = {
    def loop(acc: Int, n: Int): Int =
      if (n == 0) acc
      else loop(acc * n, n - 1)

    loop(1, n)
  }                                               //> fact: (n: Int)Int

  fact(6)                                         //> res3: Int = 720

  /////////////////////////////
  // 2. High-order functions //
  /////////////////////////////

  // Exercise 1: write a function that computes SUM(f(x)) where
  //             x are all integers between a and b: a <= x <= b
  def sum(a: Int, b: Int, f: Int => Int): Int = ???
                                                  //> sum: (a: Int, b: Int, f: Int => Int)Int

  // For example, we can compute the sum of all integers from a given range:
  def sumInts(a: Int, b: Int) = sum(a, b, (x: Int) => x)
                                                  //> sumInts: (a: Int, b: Int)Int

  // Or sume of squares:
  def sumSquares(a: Int, b: Int) = sum(a, b, (x: Int) => x * x)
                                                  //> sumSquares: (a: Int, b: Int)Int

  
  // We can generalize this example by writing a function that computes not only the sum:
  def mapReduce(mapF: Int => Int, reduceF: (Int, Int) => Int, zero: Int)(a: Int, b: Int): Int =
    if (a > b) zero
    else reduceF(mapF(a), mapReduce(mapF, reduceF, zero)(a + 1, b))
                                                  //> mapReduce: (mapF: Int => Int, reduceF: (Int, Int) => Int, zero: Int)(a: Int
                                                  //| , b: Int)Int
  mapReduce(x => x, (x, y) => x + y, 0)(1, 4)     //> res4: Int = 10

  // Or we can use parameter types to write a generic map-reduce:
  // - the elements are now passed as a list (not a range)
  // - the implementation is only a single line!
  def mapReduceG[T](mapF: T => T, reduceF: (T, T) => T, zero: T)(elems: List[T]): T =
    elems.map(mapF).fold(zero)(reduceF)           //> mapReduceG: [T](mapF: T => T, reduceF: (T, T) => T, zero: T)(elems: List[T]
                                                  //| )T

  mapReduceG[Int](x => x, (x, y) => x + y, 0)(List(1, 2, 3, 4))
                                                  //> res5: Int = 10
  mapReduceG[String](x => x.capitalize, (x, y) => x concat y, "")(List("a", "b", "c", "d"))
                                                  //> res6: String = ABCD