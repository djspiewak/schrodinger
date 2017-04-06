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

/** A type-class for `F[_]` data types that have a data constructor
  * taking a by-name value and that is equivalent to `Applicative.pure`
  * for pure expressions.
  */
trait Evaluable[F[_]] {
  /** Constructor for `F[A]` that's going to evaluate the
    * given by-name value in the `F` context, with whatever
    * properties this data-type may have (e.g. lazy, async,
    * exceptions catching, etc).
    *
    * For pure expressions, in case `F[_]` also implements the
    * `Applicative` type-class, then this operation should yield
    * a value that's equivalent with `pure(f)`.
    *
    * In case this is an applicative, should be isomorphic to:
    * {{{
    *   F.pure(()).map(_ => F.pure(f))
    * }}}
    */
  def eval[A](f: => A): F[A]
}

object Evaluable {
  /** Returns the [[Evaluable]] instance for a given `F` type. */
  @inline def apply[F[_]](implicit F: Evaluable[F]): Evaluable[F] = F
}