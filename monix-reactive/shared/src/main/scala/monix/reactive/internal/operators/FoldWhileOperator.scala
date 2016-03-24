/*
 * Copyright (c) 2014-2016 by its authors. Some rights reserved.
 * See the project homepage at: https://monix.io
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

package monix.reactive.internal.operators

import monix.execution.Ack
import monix.execution.Ack.{Stop, Continue}
import monix.reactive.observables.ObservableLike
import ObservableLike.Operator
import monix.reactive.observers.Subscriber

import scala.concurrent.Future
import scala.util.control.NonFatal

private[reactive] final
class FoldWhileOperator[A,R](initial: R, f: (R,A) => (Boolean, R))
  extends Operator[A,R] {

  def apply(out: Subscriber[R]): Subscriber[A] =
    new Subscriber[A] {
      implicit val scheduler = out.scheduler
      private[this] var isDone = false
      private[this] var state = initial

      def onNext(elem: A): Future[Ack] = {
        // Protects calls to user code from within the operator,
        // as a matter of contract.
        var streamErrors = true
        try {
          val (continue, nextState) = f(state, elem)
          streamErrors = false
          state = nextState

          if (continue) Continue else {
            onComplete()
            Stop
          }
        } catch {
          case NonFatal(ex) if streamErrors =>
            onError(ex)
            Stop
        }
      }

      def onComplete(): Unit =
        if (!isDone) {
          isDone = true
          if (out.onNext(state) ne Stop)
            out.onComplete()
        }

      def onError(ex: Throwable): Unit =
        if (!isDone) {
          isDone = true
          out.onError(ex)
        }
    }
}