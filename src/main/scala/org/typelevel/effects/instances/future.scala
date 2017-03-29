package org.typelevel.effects.instances

import org.typelevel.effects.{Async, Callback}
import scala.concurrent.{ExecutionContext, Future, OnCompleteRunnable, Promise}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/** Implementation for Scala's [[scala.concurrent.Future Future]]. */
object future extends Async[Future] {
  def create[A](cb: (Callback[A]) => Unit)(implicit ec: ExecutionContext): Future[A] = {
    val p = Promise[A]()
    ec.execute(new Runnable with OnCompleteRunnable {
      def run(): Unit = {
        try cb(Callback.fromPromise(p))
        catch { case NonFatal(ex) => ec.reportFailure(ex) }
      }
    })
    p.future
  }

  def unsafeExecuteAsyncIO[A](fa: Future[A], cb: Callback[A])
    (implicit ec: ExecutionContext): Unit = {

    fa.onComplete {
      case Success(value) =>
        cb.success(value)
      case Failure(ex) =>
        cb.failure(ex)
    }
  }

  def unsafeExecuteTrySyncIO[A](fa: Future[A], cb: Callback[A])
    (implicit ec: ExecutionContext): Either[Unit, A] = {

    fa.value match {
      case None => Left(unsafeExecuteAsyncIO(fa, cb))
      case Some(tryR) => tryR match {
        case Success(a) => Right(a)
        case Failure(_) =>
          // Treating errors asynchronously
          Left(unsafeExecuteAsyncIO(fa, cb))
      }
    }
  }
}
