/*
 * Copyright (c) 2017 by its authors. Some rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package schrodinger

import catalysts.Platform
import cats.Eq
import cats.laws.IsEq
import minitest.SimpleTestSuite
import minitest.laws.Checkers
import monix.execution.schedulers.TestScheduler
import org.scalacheck.Prop
import org.scalacheck.Test.Parameters
import org.typelevel.discipline.Laws

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait BaseLawsSuite extends SimpleTestSuite with Checkers with cats.instances.AllInstances {
  override lazy val checkConfig: Parameters =
    Parameters.default
      .withMinSuccessfulTests(if (Platform.isJvm) 100 else 10)
      .withMaxDiscardRatio(if (Platform.isJvm) 5.0f else 50.0f)
      .withMaxSize(32)

  implicit def isEqToProp[T](isEq: IsEq[T])(implicit eq: Eq[T]): Prop =
    Prop(eq.eqv(isEq.lhs, isEq.rhs))

  def checkAll(name: String)(f: TestScheduler => Laws#RuleSet) {
    implicit val ec = TestScheduler()
    for ((id, prop) ← f(ec).all.properties)
      test(name + "." + id) {
        check(prop)
      }
  }

  implicit def futureEq[A : Eq](implicit s: TestScheduler): Eq[Future[A]] =
    new Eq[Future[A]] {
      override def eqv(x: Future[A], y: Future[A]): Boolean = {
        val A = implicitly[Eq[A]]
        s.tick(1.day)

        x.value.exists {
          case Failure(_) =>
            y.value.exists {
              case Success(_) => false
              case Failure(_) => true
            }
          case Success(a) =>
            y.value.exists {
              case Success(b) => A.eqv(a, b)
              case Failure(_) => false
            }
        }
      }
    }
}
