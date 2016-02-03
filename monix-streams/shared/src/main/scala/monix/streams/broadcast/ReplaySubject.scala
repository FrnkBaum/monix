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

package monix.streams.broadcast

import monix.execution.Scheduler
import monix.streams.{broadcast, OverflowStrategy}

/**
  * Represents a [[Subject Subject]] that uses an underlying
  * [[broadcast.ReplayPipe ReplayPipe]].
  */
final class ReplaySubject[T] private
  (strategy: OverflowStrategy.Synchronous, onOverflow: Long => T, s: Scheduler)
  extends PipedSubject(ReplayPipe[T](), strategy, onOverflow, s)

object ReplaySubject {
  /**
    * Builds a [[Subject Subject]] that uses an underlying
    * [[broadcast.ReplayPipe ReplayPipe]].
    *
    * @param strategy - the [[OverflowStrategy overflow strategy]]
    *        used for buffering, which specifies what to do in case
    *        we're dealing with slow consumers: should an unbounded
    *        buffer be used, should back-pressure be applied, should
    *        the pipeline drop newer or older events, should it drop
    *        the whole buffer?  See [[OverflowStrategy]] for more
    *        details.
    */
  def apply[T](strategy: OverflowStrategy.Synchronous)
    (implicit s: Scheduler): ReplaySubject[T] = {

    new ReplaySubject[T](strategy, null, s)
  }

  /**
    * Builds a [[Subject Subject]] that uses an underlying
    * [[broadcast.ReplayPipe ReplayPipe]].
    *
    * @param strategy   - the [[OverflowStrategy overflow strategy]]
    *                   used for buffering, which specifies what to do in case
    *                   we're dealing with slow consumers: should an unbounded
    *                   buffer be used, should back-pressure be applied, should
    *                   the pipeline drop newer or older events, should it drop
    *                   the whole buffer?  See [[OverflowStrategy]] for more
    *                   details.
    * @param onOverflow - a function that is used for signaling a special
    *                   event used to inform the consumers that an overflow event
    *                   happened, function that receives the number of dropped
    *                   events as a parameter (see [[OverflowStrategy.Evicted]])
    */
  def apply[T](strategy: OverflowStrategy.Evicted, onOverflow: Long => T)
    (implicit s: Scheduler): ReplaySubject[T] = {

    new ReplaySubject[T](strategy, onOverflow, s)
  }
}
