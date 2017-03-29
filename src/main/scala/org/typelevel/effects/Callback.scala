package org.typelevel.effects

import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

/** Callback type for signaling single results in [[UnsafeIO]].
  *
  * This type would be equivalent with:
  * {{{
  *   type Callback[-A] = Try[A] => Unit
  * }}}
  *
  * This interface was preferred instead in order to avoid
  * the extra boxing.
  *
  * Usage contract:
  *
  *  - a callback's `success` or `failure` methods
  *    can only be called at most once
  *
  *  - no thread-safety guarantees are provided,
  *    implementations need synchronization if
  *    there are thread-safety concerns
  *
  * Example:
  * {{{
  *   val cb = new Callback[String] {
  *     def success(value: String): Unit =
  *       println(s"Message: $value")
  *     def failure(ex: Throwable): Unit =
  *       System.err.println(s"Error: $ex")
  *   }
  *
  *   // Later ...
  *   cb.success("Hello, world!")
  * }}}
  */
trait Callback[-A] {
  def success(value: A): Unit
  def failure(ex: Throwable): Unit
}

object Callback {
  /** Converts a callback function that receives a
    * [[scala.util.Try]] argument into a [[Callback]].
    */
  def fromTryFn[A](f: Try[A] => Unit): Callback[A] =
    new Callback[A] {
      def success(value: A): Unit = f(Success(value))
      def failure(ex: Throwable): Unit = f(Failure(ex))
    }

  /** Converts a callback function that receives a
    * [[scala.Either]] argument into a [[Callback]].
    */
  def fromEitherFn[A](f: Either[Throwable, A] => Unit): Callback[A] =
    new Callback[A] {
      def success(value: A): Unit = f(Right(value))
      def failure(ex: Throwable): Unit = f(Left(ex))
    }

  /** Converts any Scala [[scala.concurrent.Promise Promise]]
    * into a [[Callback]].
    */
  def fromPromise[A](p: Promise[A]): Callback[A] =
    new Callback[A] {
      def success(value: A): Unit = p.success(value)
      def failure(ex: Throwable): Unit = p.failure(ex)
    }
}
