package org.typelevel.effects

import org.typelevel.effects.instances.AsyncFuture

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/** Type-class describing `F[_]` data types capable of executing
  * asynchronous computations that produce a single result.
  */
trait Async[F[_]] extends Effect[F] {
  /** Creates an `F[A]` instance from a provided function
    * that will have a callback injected for
    * signaling the final result.
    *
    * Example:
    * {{{
    *   Async[Future].create[String] { cb =>
    *     cb(Right("Hello, world!"))
    *   }
    * }}}
    *
    * @param f is a function that will be called with a callback
    *          for signaling the result once it is ready
    */
  def create[A](f: (Either[Throwable, A] => Unit) => Unit): F[A]
}

object Async {
  /** Returns the [[Async]] instance for a given `F` type. */
  def apply[F[_]](implicit F: Async[F]): Async[F] = F

  /** Default [[Async]] implementation for Scala's [[scala.concurrent.Future Future]]. */
  implicit def futureInstance(implicit ec: ExecutionContext): Async[Future] =
    new AsyncFuture()
}
