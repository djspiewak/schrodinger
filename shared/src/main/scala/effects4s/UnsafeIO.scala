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

/** Common OOP interface for effects-triggering abstractions,
  * like `IO`, `Task`, `Future`, etc.
  */
trait UnsafeIO[+A] extends Any {
  /** Return type for results of `unsafeExecuteIO`.
    *
    * This is usually `Unit`, because not much can happen after
    * triggering the evaluation of an `UnsafeIO` reference, but
    * depending on the implementation this could also be a handle
    * allowing for metrics gathering or for cancellation of a
    * running task.
    */
  type Handle = Unit

  /** Mirrors [[Effect.unsafeExtractAsync]]. */
  def unsafeExtractAsync(cb: Either[Throwable, A] => Unit): Handle

  /** Mirrors [[Effect.unsafeExtractTrySync]]. */
  def unsafeExtractTrySync(cb: Either[Throwable, A] => Unit): Either[Handle, A]
}
