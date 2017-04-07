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

import cats.{Applicative, ApplicativeError}
import cats.laws._
import cats.syntax.all._

/** Laws that have to be satisfied by [[schrodinger.Evaluable]], assuming
  * that the `F[_]` data-type is also a [[cats.Applicative]].
  */
trait EvaluableLaws[F[_]] {
  implicit def F: Evaluable[F]
  implicit def G: Applicative[F]

  def evalEquivalenceWithPure[A](a: A): IsEq[F[A]] =
    F.eval(a) <-> G.pure(a)

  def evalConsistentWithPureMapped[A, B](a: A, f: A => B): IsEq[F[B]] =
    F.eval(f(a)) <-> G.pure(a).map(f)

  def evalCapturesExceptions[A](ex: Throwable)
    (implicit E: ApplicativeError[F, Throwable]): IsEq[F[A]] =
    F.eval[A](throw ex) <-> E.raiseError[A](ex)
}

object EvaluableLaws {
  /** Data-type builder for [[EvaluableLaws]]. */
  def apply[F[_]](implicit ev1: Evaluable[F], ev2: Applicative[F]): EvaluableLaws[F] =
    new EvaluableLaws[F] {
      implicit val F: Evaluable[F] = ev1
      implicit val G: Applicative[F] = ev2
    }
}