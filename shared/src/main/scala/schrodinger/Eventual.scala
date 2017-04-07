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

import schrodinger.instances.AllEventualInstances

import scala.annotation.implicitNotFound

/** Type-class describing `F[_]` data types capable of evaluating
  * side-effects in the `F` context and that can signal a single
  * value or error as the result, potentially asynchronous.
  */
@implicitNotFound("""Cannot find implicit value for Eventual[${F}].
Building this implicit value might depend on having an implicit
s.c.ExecutionContext in scope or some other equivalent type.""")
trait Eventual[F[_]] extends Evaluable[F] {
  /** Extracts the `A` value out of the `F[_]` context, where
    * that value can be the result of an asynchronous computation.
    *
    * @param fa represents the computation to execute, potentially async
    * @param cb is the callback that will get called once a result is ready
    */
  def unsafeExtractAsync[A](fa: F[A])(cb: Either[Throwable, A] => Unit): Unit

  /** Extracts the `A` value out of the `F[_]` context, where
    * that value can be the result of an asynchronous computation,
    * returning an immediate result if the underlying computation
    * yielded an immediate (synchronous) result.
    *
    * This operation is an optimization on [[unsafeExtractAsync]]
    * because for implementations capable of returning an immediate
    * result there's no need to force an async boundary.
    *
    * Note that in case this method needs to return an error
    * (described as a `Throwable`), then that error should be returned
    * asynchronously (by means of the provided callback), that
    * callback being the only legal way to signal exceptions when
    * calling this method.
    *
    * @param cb is the callback that will get called once a result is
    *        ready, if the execution was asynchronous
    *
    * @return `Left` in case the execution was asynchronous, in which
    *         case the caller needs to wait for the result to be
    *         signaled by the provided callback, or `Right(a)` in case
    *         the result is available immediately (without further
    *         async execution)
    */
  def unsafeExtractTrySync[A](fa: F[A])(cb: Either[Throwable, A] => Unit): Either[Unit, A]
}

object Eventual extends AllEventualInstances[Eventual] {
  /** Returns the [[Async]] instance for a given `F` type. */
  def apply[F[_]](implicit F: Async[F]): Async[F] = F
}