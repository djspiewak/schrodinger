package org.typelevel.effects

import org.typelevel.effects.instances.AsyncFuture

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/** Type-class describing `F[_]` data types capable of evaluating side-effects
  * in the `F` context and that can signal a single value or error as the result,
  * potentially asynchronous.
  */
trait Effect[F[_]] {
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

    def unsafeExtractAsync(cb: (Either[Throwable, A]) => Unit): Unit =
      F.unsafeExtractAsync(fa)(cb)
    def unsafeExtractTrySync(cb: (Either[Throwable, A]) => Handle): Either[Handle, A] =
      F.unsafeExtractTrySync(fa)(cb)
  }

  /** Default [[Effect]] implementation for Scala's [[scala.concurrent.Future Future]]. */
  implicit def futureInstance(implicit ec: ExecutionContext): Effect[Future] =
    new AsyncFuture()
}