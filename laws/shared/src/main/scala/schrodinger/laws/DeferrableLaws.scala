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

import cats.Monad
import cats.laws._
import cats.syntax.all._
import schrodinger.laws.DeferrableLaws.StatefulBox

/** Laws that have to be passed by [[schrodinger.Deferrable]], assuming
  * that the `F[_]` data-type is also a [[cats.Monad]].
  */
trait DeferrableLaws[F[_]] extends EvaluableLaws[F] {
  implicit def F: Deferrable[F]
  implicit def G: Monad[F]

  def evalEquivalenceWithDefer[A, B](a: A, f: A => B): IsEq[F[B]] =
    F.eval(f(a)) <-> F.defer(G.pure(f(a)))

  def evalRepeatsSideEffects[A, B](a: A, b: B, f: (A, B) => A): IsEq[F[A]] = {
    val state = new StatefulBox(a)
    val fa = F.eval(state.transform(a => f(a, b)))
    fa.flatMap(_ => fa) <-> G.pure(f(f(a, b), b))
  }

  def deferRepeatsSideEffects[A, B](a: A, b: B, f: (A, B) => A): IsEq[F[A]] = {
    val state = new StatefulBox(a)
    val fa = F.defer(G.pure(state.transform(a => f(a, b))))
    fa.flatMap(_ => fa) <-> G.pure(f(f(a, b), b))
  }

  lazy val flatMapStackSafety: IsEq[F[Int]] = {
    // tailRecM expressed with flatMap
    def loop[A, B](a: A)(f: A => F[Either[A, B]]): F[B] =
      G.flatMap(f(a)) {
        case Right(b) =>
          G.pure(b)
        case Left(nextA) =>
          loop(nextA)(f)
      }

    val n = 50000
    val res = loop(0)(i => G.pure(if (i < n) Either.left(i + 1) else Either.right(i)))
    res <-> G.pure(n)
  }
}

object DeferrableLaws {
  /** Data-type builder for [[DeferrableLaws]]. */
  def apply[F[_]](implicit ev1: Deferrable[F], ev2: Monad[F]): DeferrableLaws[F] =
    new DeferrableLaws[F] {
      implicit val F: Deferrable[F] = ev1
      implicit val G: Monad[F] = ev2
    }

  /**
    * A boxed and synchronized variable to use for
    * testing deferred side effects.
    */
  final class StatefulBox[A](initial: A) {
    private[this] var state = initial

    def get: A =
      synchronized(state)

    def transform(f: A => A): A =
      synchronized{ state = f(state); state }
  }
}
