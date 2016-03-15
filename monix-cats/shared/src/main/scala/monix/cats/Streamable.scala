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

package monix.cats

import _root_.cats.MonadFilter
import monix.cats.internal.{DistinctByKeyState, DistinctState}
import simulacrum.typeclass
import scala.collection.immutable.Queue
import scala.language.{higherKinds, implicitConversions}

@typeclass trait Streamable[F[_]]
  extends MonadConsError[F,Throwable] with Recoverable[F,Throwable]
  with MonadFilter[F] with Filtered[F] with Scannable[F] with FFoldable[F]
  with Zippable[F] {

  /** Lifts any `Iterable` into a `Sequenceable` type. */
  def fromIterable[A](ia: Iterable[A]): F[A]

  /** Creates a sequence that eliminates duplicates from the source. */
  def distinct[A](fa: F[A]): F[A] = {
    val set = scan(fa, Set.empty[A])((acc, elem) => acc + elem)
    flatMap(set)(fromIterable)
  }

  /** Creates a sequence that eliminates duplicates from the source,
    * as determined by the given selector function that returns keys
    * for comparison.
    */
  def distinctByKey[A,Key](fa: F[A])(key: A => Key): F[A] = {
    val scanned = scan(fa, (Queue.empty[A], Set.empty[Key])) { (acc, elem) =>
      val (queue, set) = acc
      val id = key(elem)
      if (set(id)) acc else
        (queue.enqueue(elem), set + id)
    }

    flatMap(map(scanned)(_._1))(fromIterable)
  }

  /** Suppress duplicate consecutive items emitted by the source. */
  def distinctUntilChanged[A](fa: F[A]): F[A] = {
    val scanned = scan(fa, null : DistinctState[A]) { (previous, elem) =>
      if (previous == null || previous.state != elem)
        DistinctState.Emit(elem)
      else
        DistinctState.Wait(elem)
    }

    collect(scanned) { case DistinctState.Emit(elem) => elem }
  }

  /** Suppress duplicate consecutive items emitted by the source. */
  def distinctUntilChangedByKey[A,Key](fa: F[A])(key: A => Key): F[A] = {
    val scanned = scan(fa, null : DistinctByKeyState[A,Key]) { (previous, elem) =>
      val newKey = key(elem)
      if (previous == null || previous.key != newKey)
        DistinctByKeyState.Emit(elem, newKey)
      else
        DistinctByKeyState.Wait(elem, newKey)
    }

    collect(scanned) { case DistinctByKeyState.Emit(elem,_) => elem }
  }

  /** Returns a new sequence that will drop a maximum of
    * `n` elements from the start of the source sequence.
    */
  def drop[A](fa: F[A], n: Int): F[A]

  /** Drops the last `n` elements (from the end). */
  def dropLast[A](fa: F[A], n: Int): F[A]

  /** Returns a new sequence that will drop elements from
    * the start of the source sequence, for as long as the given
    * function `f` returns `true` and then stop.
    */
  def dropWhile[A](fa: F[A])(f: A => Boolean): F[A]

  /** Returns the first element in a sequence. */
  def headF[A](fa: F[A]): F[A] =
    take(fa, 1)

  /** Returns the first element in a sequence. */
  def headOrElseF[A](fa: F[A])(default: => A): F[A] =
    map(foldLeftF(take(fa, 1), Option.empty[A])((_,a) => Some(a))) {
      case None => default
      case Some(a) => a
    }

  /** Returns the first element in a sequence.
    *
    * Alias for [[headOrElseF]].
    */
  def firstOrElseF[A](fa: F[A])(default: => A): F[A] =
    headOrElseF(fa)(default)

  /** Returns the last element in a sequence. */
  def lastF[A](fa: F[A]): F[A] =
    takeLast(fa, 1)

  /** Returns a new sequence with the first element dropped. */
  def tail[A](fa: F[A]): F[A] = drop(fa, 1)

  /** Returns a new sequence that will take a maximum of
    * `n` elements from the start of the source sequence.
    */
  def take[A](fa: F[A], n: Int): F[A]

  /** Returns a new sequence that will take a maximum of
    * `n` elements from the end of the source sequence.
    */
  def takeLast[A](fa: F[A], n: Int): F[A]

  /** Returns a new sequence that will take elements from
    * the start of the source sequence, for as long as the given
    * function `f` returns `true` and then stop.
    */
  def takeWhile[A](fa: F[A])(f: A => Boolean): F[A]

  /** Periodically gather items emitted by the source into bundles
    * of the specified size.
    */
  def buffer[A](fa: F[A])(count: Int): F[Seq[A]] =
    bufferSkipped(fa, count, count)

  /** Periodically gather items emitted by the source into bundles.
    *
    * For `count` and `skip` there are 3 possibilities:
    *
    *  1. in case `skip == count`, then there are no items dropped and
    *      no overlap, the call being equivalent to `buffer(count)`
    *  2. in case `skip < count`, then overlap between buffers
    *     happens, with the number of elements being repeated being
    *     `count - skip`
    *  3. in case `skip > count`, then `skip - count` elements start
    *     getting dropped between windows
    */
  def bufferSkipped[A](fa: F[A], count: Int, skip: Int): F[Seq[A]]

  /** Ends the sequence with the given elements. */
  def endWith[A](fa: F[A])(elems: A*): F[A] =
    followWith(fa)(fromIterable(elems))

  /** Starts the sequence with the given elements. */
  def startWith[A](fa: F[A])(elems: A*): F[A] =
    followWith(fromIterable(elems))(fa)

  /** Given an `Ordering` returns the maximum element of the source. */
  def maxF[A](fa: F[A])(implicit A: Ordering[A]): F[A] = {
    val folded = foldLeftF(fa, Option.empty[A]) {
      case (None, a) => Some(a)
      case (Some(max), a) => Some(if (A.compare(max, a) < 0) a else max)
    }

    collect(folded) { case Some(max) => max }
  }

  /** Given a key extractor, finds the element with the maximum key. */
  def maxByF[A,B](fa: F[A])(f: A => B)(implicit B: Ordering[B]): F[A] = {
    val folded = foldLeftF(fa, Option.empty[(A,B)]) {
      case (None, a) => Some((a, f(a)))
      case (ref @ Some((prev,max)), elem) =>
        val newMax = f(elem)
        if (B.compare(max, newMax) < 0)
          Some((elem, newMax))
        else
          ref
    }

    collect(folded) { case Some((a,_)) => a }
  }

  /** Given an `Ordering` returns the maximum element of the source. */
  def minF[A](fa: F[A])(implicit A: Ordering[A]): F[A] = {
    val folded = foldLeftF(fa, Option.empty[A]) {
      case (None, a) => Some(a)
      case (Some(min), a) => Some(if (A.compare(min, a) > 0) a else min)
    }

    collect(folded) { case Some(min) => min }
  }

  /** Given a key extractor, finds the element with the minimum key. */
  def minByF[A,B](fa: F[A])(f: A => B)(implicit B: Ordering[B]): F[A] = {
    val folded = foldLeftF(fa, Option.empty[(A,B)]) {
      case (None, a) => Some((a, f(a)))
      case (ref @ Some((prev,min)), elem) =>
        val newMin = f(elem)
        if (B.compare(min, newMin) > 0)
          Some((elem, newMin))
        else
          ref
    }

    collect(folded) { case Some((a,_)) => a }
  }
}