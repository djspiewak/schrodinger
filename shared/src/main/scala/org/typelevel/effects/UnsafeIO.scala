package org.typelevel.effects

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
