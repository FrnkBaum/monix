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

package monix.reactive

/** The `MulticastStrategy` specifies the strategy for
  * building data-sources that are shared between multiple subscribers.
  *
  * By default observables tend to be cold.
  */
sealed abstract class MulticastStrategy[+A]

/** The [[MulticastStrategy]] enumerated.
  *
  * @define publish The `Publish` strategy is for emitting to a subscriber
  *         only those items that are emitted by the source subsequent
  *         to the time of the subscription.
  *
  *         Corresponds to [[Pipe.publish]].
  *
  * @define behavior The `Behavior` strategy is for building multicast observables that
  *         emit the most recently emitted item by the source before the
  *         source starts being mirrored.
  *
  *         Corresponds to [[Pipe.behavior]].
  *
  * @define async The `Async` strategy is for building multicast observables that
  *         emit the last value (and only the last value) of the source
  *         and only after the source completes.
  *
  *         Corresponds to [[Pipe.async]].
  *
  * @define replay The `Replay` strategy is for building multicast observables
  *         that repeat all the generated items by the source, regardless of
  *         when the source is subscribed.
  *
  *         Corresponds to [[Pipe.replay]].
  *
  * @define replayPopulated The `ReplayPopulated` strategy is for building multicast observables
  *         that repeat all the generated items by the source, regardless of
  *         when the source is subscribed, with the prepended `initial`
  *         sequence of values.
  *
  *         Corresponds to [[Pipe.replayPopulated]].
  *
  * @define replayLimited The `ReplayLimited` strategy is for building multicast
  *         observables that repeat the generated items by the source, but limited by the
  *         maximum size of the underlying buffer.
  *
  *         When maximum size is reached, the underlying buffer starts dropping
  *         older events. Note that the size of the resulting buffer is not necessarily
  *         the given capacity, as the implementation may choose to increase it for optimisation
  *         purposes.
  *
  *         Corresponds to [[Pipe.replayLimited]].
  */
object MulticastStrategy {
  /** $publish */
  def publish[A]: MulticastStrategy[A] = Publish

  /** $publish */
  case object Publish extends MulticastStrategy[Nothing]

  /** $behavior */
  def behavior[A](initial: A): MulticastStrategy[A] = Behavior(initial)

  /** $behavior */
  case class Behavior[A](initial: A) extends MulticastStrategy[A]

  /** $create */
  def async[A]: MulticastStrategy[A] = Async

  /** $create */
  case object Async extends MulticastStrategy[Nothing]

  /** $replay */
  def replay[A]: MulticastStrategy[A] = Replay

  /** $replay */
  case object Replay extends MulticastStrategy[Nothing]

  /** $replayPopulated */
  def replayPopulated[A](initial: Seq[A]): MulticastStrategy[A] = ReplayPopulated(initial)

  /** $replayPopulated */
  case class ReplayPopulated[A](initial: Seq[A]) extends MulticastStrategy[A]

  /** $replayLimited */
  def replayLimited[A](capacity: Int): MulticastStrategy[A] = ReplayLimited(capacity)

  /** $replayLimited */
  case class ReplayLimited(capacity: Int) extends MulticastStrategy[Nothing]
}
