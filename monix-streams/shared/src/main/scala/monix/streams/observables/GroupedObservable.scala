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

package monix.streams.observables

import scala.language.reflectiveCalls
import monix.execution.{Cancelable, Scheduler}
import monix.streams.{Subscriber, Observer, Observable, Ack}
import monix.streams.internal._
import monix.streams.observers.CacheUntilConnectSubscriber
import scala.concurrent.Future

/** A `GroupedObservable` is an observable type generated
  * by `Observable.groupBy`. It has the following properties:
  *
  * - comes accompanied with a `key` property after which
  *   the grouping was made
  *
  * - supports a single subscriber, throwing `IllegalStateException`
  *   if you attempt multiple subscriptions
  */
trait GroupedObservable[K, +V] extends Observable[V]
  with LiftOperators[V, ({type λ[+α] = GroupedObservable[K, α]})#λ] { self =>

  /** Returns the key associated with this grouped observable. */
  def key: K

  protected def liftToSelf[U](f: (Observable[V]) => Observable[U]): GroupedObservable[K, U] =
    new GroupedObservable[K, U] {
      val key = self.key

      private[this] val lifted = f(self)
      def unsafeSubscribeFn(subscriber: Subscriber[U]): Unit =
        lifted.unsafeSubscribeFn(subscriber)
    }
}

object GroupedObservable {
  /** Builder returning an input+output pair */
  private[monix] def broadcast[K,V](key: K, onCancel: Cancelable)
    (implicit s: Scheduler): (Subscriber[V], GroupedObservable[K,V]) = {

    val ref = new Implementation[K,V](key, onCancel)
    (ref, ref)
  }

  /** Implementation for [[GroupedObservable]] */
  private final class Implementation[K, V](val key: K, onCancel: Cancelable)
    (implicit val scheduler: Scheduler)
    extends GroupedObservable[K,V] with Subscriber[V] { self =>

    // needs to be set upon subscription
    private[this] var ref: Subscriber[V] = null
    private[this] val underlying = {
      val o = new Observer[V] {
        def onNext(elem: V) = {
          val downstream = if (ref == null) self.synchronized(ref) else ref
          downstream.onNext(elem)
            .ifCanceledDoCancel(onCancel)
        }

        def onError(ex: Throwable): Unit = {
          val downstream = if (ref == null) self.synchronized(ref) else ref
          downstream.onError(ex)
        }

        def onComplete(): Unit = {
          val downstream = if (ref == null) self.synchronized(ref) else ref
          downstream.onComplete()
        }
      }

      CacheUntilConnectSubscriber(Subscriber(o, scheduler))
    }

    def onNext(elem: V): Future[Ack] = underlying.onNext(elem)
    def onError(ex: Throwable): Unit = underlying.onError(ex)
    def onComplete(): Unit = underlying.onComplete()

    def unsafeSubscribeFn(subscriber: Subscriber[V]): Unit =
      self.synchronized {
        if (ref != null) {
          subscriber.onError(
            new IllegalStateException(
              s"Cannot subscribe twice to a GroupedObservable"))
        }
        else {
          ref = subscriber
          underlying.connect()
        }
      }
  }
}
