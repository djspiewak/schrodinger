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
package instances

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/** Default instances for Scala's [[scala.concurrent.Future Future]]. */
class FutureInstances(implicit ec: ExecutionContext)
  extends Effect[Future] {

  override def eval[A](f: => A): Future[A] =
    Future(f)

  override def unsafeExtractAsync[A](fa: Future[A])(cb: (Either[Throwable, A]) => Unit): Unit =
    fa.onComplete {
      case Success(v) => Right(v)
      case Failure(ex) => Left(ex)
    }

  override def unsafeExtractTrySync[A](fa: Future[A])(cb: (Either[Throwable, A]) => Unit): Either[Unit, A] =
    fa.value match {
      case Some(Success(v)) => Right(v)
      case _ => Left(unsafeExtractAsync(fa)(cb))
    }
}
