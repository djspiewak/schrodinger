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

package effects4s
package laws
package discipline

import catalysts.Platform
import cats.{ApplicativeError, Eq, Monad}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Prop}

/** Tests that have to be passed by [[effects4s.Deferrable]], assuming
  * that the `F[_]` data-type is also a [[cats.Monad]].
  */
trait DeferrableTests[F[_]] extends EvaluableTests[F] {
  import cats.laws.discipline.catsLawsIsEqToProp

  def laws: DeferrableLaws[F]

  def deferrable[A: Arbitrary, B: Arbitrary](implicit
    AtoB: Arbitrary[A => B],
    ABtoA: Arbitrary[(A, B) => A],
    EqFA: Eq[F[A]],
    EqFB: Eq[F[B]],
    EqFInt: Eq[F[Int]]): RuleSet = {

    new RuleSet {
      def name: String = "deferrable"
      def bases: Seq[(String, RuleSet)] = Nil
      def parents: Seq[RuleSet] = Seq(evaluable[A, B])
      def props: Seq[(String, Prop)] = Seq(
        "eval equivalence with defer" -> forAll(laws.evalEquivalenceWithDefer[A, B] _),
        "eval repeats side effects" -> forAll(laws.evalRepeatsSideEffects[A, B] _),
        "defer repeats side effects" -> forAll(laws.deferRepeatsSideEffects[A, B] _)
      ) ++ (if (Platform.isJvm) Seq[(String, Prop)]("flatMap stack safety" -> Prop.lzy(laws.flatMapStackSafety)) else Seq.empty)
    }
  }

  def deferrableWithError[A: Arbitrary, B: Arbitrary](implicit
    AtoB: Arbitrary[A => B],
    ABtoA: Arbitrary[(A, B) => A],
    EqFA: Eq[F[A]],
    EqFB: Eq[F[B]],
    EqFInt: Eq[F[Int]],
    apErr: ApplicativeError[F, Throwable]): RuleSet = {

    new RuleSet {
      def name: String = "deferrableWithError"
      def bases: Seq[(String, RuleSet)] = Nil
      def parents: Seq[RuleSet] = Seq(deferrable[A, B], evaluableWithError[A, B])
      def props: Seq[(String, Prop)] = Seq.empty
    }
  }
}

object DeferrableTests {
  /** Tests that have to be passed by [[effects4s.Deferrable]], assuming
    * that the `F[_]` data-type is also a [[cats.Monad]].
    */
  def apply[F[_] : Deferrable : Monad]: DeferrableTests[F] =
    new DeferrableTests[F] {
      override val laws: DeferrableLaws[F] =
        DeferrableLaws[F]
    }
}