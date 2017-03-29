package org.typelevel.effects

import scala.concurrent.ExecutionContext

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

  /** Mirrors [[Async.unsafeExecuteAsyncIO]]. */
  def unsafeExecuteAsyncIO(cb: Callback[A])(implicit ec: ExecutionContext): Handle

  /** Mirrors [[Async.unsafeExecuteTrySyncIO]]. */
  def unsafeExecuteTrySyncIO(cb: Callback[A])(implicit ec: ExecutionContext): Either[Handle, A]
}
