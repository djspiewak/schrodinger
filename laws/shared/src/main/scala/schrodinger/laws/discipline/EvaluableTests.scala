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
package laws
package discipline

import cats.{Applicative, ApplicativeError, Eq}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import org.typelevel.discipline.Laws

/** Tests that have to be passed by [[schrodinger.Evaluable]], assuming
  * that the `F[_]` data-type is also a [[cats.Applicative]].
  */
trait EvaluableTests[F[_]] extends Laws {
  import cats.laws.discipline.catsLawsIsEqToProp

  def laws: EvaluableLaws[F]

  def evaluable[A: Arbitrary, B: Arbitrary](implicit
    AtoB: Arbitrary[A => B],
    EqFA: Eq[F[A]],
    EqFB: Eq[F[B]]): RuleSet = {

    new DefaultRuleSet(
      name = "evaluable",
      parent = None,
      "eval consistent with pure" -> forAll(laws.evalEquivalenceWithPure[A] _),
      "eval consistent with pure mapped" -> forAll(laws.evalConsistentWithPureMapped[A, B] _)
    )
  }

  def evaluableWithError[A: Arbitrary, B: Arbitrary](implicit
    AtoB: Arbitrary[A => B],
    EqFA: Eq[F[A]],
    EqFB: Eq[F[B]],
    apErr: ApplicativeError[F, Throwable]): RuleSet = {

    new DefaultRuleSet(
      name = "applicativeEvalWithError",
      parent = Some(evaluable[A, B]),
      "eval captures exceptions" -> forAll(laws.evalCapturesExceptions[A] _)
    )
  }
}

object EvaluableTests {
  /** Data-type builder for [[EvaluableTests]]. */
  def apply[F[_] : Evaluable : Applicative]: EvaluableTests[F] =
    new EvaluableTests[F] {
      override val laws: EvaluableLaws[F] =
        EvaluableLaws[F]
    }
}
