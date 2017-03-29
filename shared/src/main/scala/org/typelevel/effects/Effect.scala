package org.typelevel.effects

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/** Type-class describing `F[_]` data types capable of evaluating side-effects
  * in the `F` context and that can signal a single value or error as the result,
  * potentially asynchronous.
  *
  * @define executionContextNote An
  *         [[scala.concurrent.ExecutionContext ExecutionContext]] is
  *         needed, depending on the implementation, for evaluation or
  *         for ensuring the stack safety of this operation. Note that
  *         its usage is entirely implementation dependent, so just
  *         because it is provided, that does not mean that the source
  *         will use it.
  */
trait Effect[F[_]] {
  /** Triggers the execution (evaluation) of the given `fa`.
    *
    * $executionContextNote
    *
    * @param fa represents the computation to execute, potentially async
    * @param cb is the callback that will get called once a result is ready
    * @param ec is the execution context to use for forking or ensuring stack safety
    */
  def unsafeExecuteAsyncIO[A](fa: F[A], cb: Callback[A])
    (implicit ec: ExecutionContext): Unit

  /** Triggers the execution (evaluation) of the source, potentially
    * returning an immediate result in case the evaluation was
    * synchronous.
    *
    * This operation is an optimization on [[unsafeExecuteAsyncIO]]
    * because for implementations capable of returning an immediate
    * result there's no need to force an async boundary.
    *
    * Note that in case this method needs to return an error
    * (described as a `Throwable`), then that error should be returned
    * asynchronously (by means of the provided callback), that
    * callback being the only legal way to signal exceptions when
    * calling this method.
    *
    * $executionContextNote
    *
    * @param cb is the callback that will get called once a result is
    *        ready, if the execution was asynchronous
    * @param ec is the execution context to use for forking or
    *        ensuring stack safety
    *
    * @return `Left` in case the execution was asynchronous, in which
    *         case the caller needs to wait for the result to be
    *         signaled by the provided callback, or `Right(a)` in case
    *         the result is available immediately (without further
    *         async execution)
    */
  def unsafeExecuteTrySyncIO[A](fa: F[A], cb: Callback[A])
    (implicit ec: ExecutionContext): Either[Unit, A]

  /** Transforms any `F[A]` to an [[UnsafeIO]] implementation. */
  def toUnsafeIO[A](fa: F[A]): UnsafeIO[A] =
    new Effect.DefaultUnsafeIO[F, A](fa)(this)
}

object Effect {
  /** Returns the [[Async]] instance for a given `F` type. */
  def apply[F[_]](implicit F: Async[F]): Async[F] = F

  /** Generic implementation to use by default in [[Effect.toUnsafeIO]]. */
  final class DefaultUnsafeIO[F[_], A](fa: F[A])(implicit F: Effect[F])
    extends UnsafeIO[A] {

    def unsafeExecuteAsyncIO(cb: Callback[A])
      (implicit ec: ExecutionContext): Unit =
      F.unsafeExecuteAsyncIO(fa, cb)

    def unsafeExecuteTrySyncIO(cb: Callback[A])
      (implicit ec: ExecutionContext): Either[Unit, A] =
      F.unsafeExecuteTrySyncIO(fa, cb)
  }

  /** Default [[Effect]] implementation for Scala's [[scala.concurrent.Future Future]]. */
  implicit val futureInstance: Effect[Future] =
    instances.future
}