package cats
package tests

import cats.data.{Xor, Ior}
import cats.laws.discipline.{BifunctorTests, TraverseTests, MonadTests, SerializableTests}
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._

class IorTests extends CatsSuite {
  checkAll("Ior[String, Int]", MonadTests[String Ior ?].monad[Int, Int, Int])
  checkAll("Monad[String Ior ?]]", SerializableTests.serializable(Monad[String Ior ?]))

  checkAll("Ior[String, Int] with Option", TraverseTests[String Ior ?].traverse[Int, Int, Int, Int, Option, Option])
  checkAll("Traverse[String Ior ?]", SerializableTests.serializable(Traverse[String Ior ?]))
  checkAll("? Ior ?", BifunctorTests[Ior].bifunctor[Int, Int, Int, String, String, String])

  test("left Option is defined left and both") {
    forAll { (i: Int Ior String) =>
      (i.isLeft || i.isBoth) should === (i.left.isDefined)
    }
  }

  test("right Option is defined for right and both") {
    forAll { (i: Int Ior String) =>
      (i.isRight || i.isBoth) should === (i.right.isDefined)
    }
  }

  test("onlyLeftOrRight") {
    forAll { (i: Int Ior String) =>
      i.onlyLeft.map(Xor.left).orElse(i.onlyRight.map(Xor.right)) should === (i.onlyLeftOrRight)
    }
  }

  test("onlyBoth consistent with left and right") {
    forAll { (i: Int Ior String) =>
      i.onlyBoth should === (for {
        left <- i.left
        right <- i.right
      } yield (left, right))
    }
  }

  test("pad") {
    forAll { (i: Int Ior String) =>
      i.pad should === ((i.left, i.right))
    }
  }

  test("unwrap consistent with isBoth") {
    forAll { (i: Int Ior String) =>
      i.unwrap.isRight should === (i.isBoth)
    }
  }

  test("isLeft consistent with toOption") {
    forAll { (i: Int Ior String) =>
      i.isLeft should === (i.toOption.isEmpty)
    }
  }

  test("isLeft consistent with toList") {
    forAll { (i: Int Ior String) =>
      i.isLeft should === (i.toList.isEmpty)
    }
  }

  test("isLeft consistent with forall and exists") {
    forAll { (i: Int Ior String, p: String => Boolean) =>
      whenever(i.isLeft) {
        (i.forall(p) && !i.exists(p)) should === (true)
      }
    }
  }

  test("leftMap then swap equivalent to swap then map") {
    forAll { (i: Int Ior String, f: Int => Double) =>
      i.leftMap(f).swap should === (i.swap.map(f))
    }
  }

  test("foreach is noop for left") {
    forAll { (i: Int) =>
      Ior.left[Int, String](i).foreach { _ => fail("should not be called") }
    }
  }

  test("foreach runs for right and both") {
    forAll { (i: Int Ior String) =>
      whenever(i.isRight || i.isBoth) {
        var count = 0
        i.foreach { _ => count += 1 }
        count should === (1)
      }
    }
  }

  test("show isn't empty") {
    val iorShow = implicitly[Show[Int Ior String]]

    forAll { (i: Int Ior String) =>
      iorShow.show(i).nonEmpty should === (true)
    }
  }

  test("append left") {
    forAll { (i: Int Ior String, j: Int Ior String) =>
      i.append(j).left should === (i.left.map(_ + j.left.getOrElse(0)).orElse(j.left))
    }
  }

  test("append right") {
    forAll { (i: Int Ior String, j: Int Ior String) =>
      i.append(j).right should === (i.right.map(_ + j.right.getOrElse("")).orElse(j.right))
    }
  }
}
