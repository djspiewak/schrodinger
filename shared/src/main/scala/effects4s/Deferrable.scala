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

/** Describes data types that allows for arbitrarily delaying the
  * evaluation of an operation, triggering its execution on each run.
  *
  * Instances of this type-class have the following properties:
  *
  *  - suspend any side-effects for later, until evaluated
  *  - suspension has `always` semantics, meaning that on each
  *    evaluation of `F[_]`, any captured side-effects get repeated
  *
  * If the data type also has a Monad implementation, then this operation
  * is isomorphic to:
  * {{{
  *   F.pure(()).flatMap(_ => fa)
  * }}}
  */
trait Deferrable[F[_]] extends Evaluable[F] {
  /**
    * Returns an `F[A]` that evaluates the provided by-name `fa`
    * parameter on each run. In essence it builds an `F[A]` factory.
    */
  def defer[A](fa: => F[A]): F[A]
}

object Deferrable {
  /** Returns the [[Deferrable]] instance for a given `F` type. */
  @inline def apply[F[_]](implicit F: Deferrable[F]): Deferrable[F] = F
}