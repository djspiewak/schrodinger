package org.typelevel.effects.instances

import org.typelevel.effects.Async
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/** Implementation for Scala's [[scala.concurrent.Future Future]]. */
class AsyncFuture(implicit ec: ExecutionContext) extends Async[Future] {
  def create[A](f: (Either[Throwable, A] => Unit) => Unit): Future[A] = {
    val p = Promise[A]()
    ec.execute(new Runnable {
      def run(): Unit = {
        try f {
          case Right(value) => p.success(value)
          case Left(ex) => p.failure(ex)
        }
        catch { case NonFatal(ex) =>
          ec.reportFailure(ex)
        }
      }
    })
    p.future
  }

  def unsafeExtractAsync[A](fa: Future[A])(cb: (Either[Throwable, A]) => Unit): Unit =
    fa.onComplete {
      case Success(v) => cb(Right(v))
      case Failure(ex) => cb(Left(ex))
    }

  def unsafeExtractTrySync[A](fa: Future[A])(cb: (Either[Throwable, A]) => Unit): Either[Unit, A] =
    fa.value match {
      case Some(Success(value)) => Right(value)
      case _ => Left(unsafeExtractAsync(fa)(cb))
    }
}
