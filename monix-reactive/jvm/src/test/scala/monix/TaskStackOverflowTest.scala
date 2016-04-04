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

package monix

import minitest.TestSuite
import monix.execution.Scheduler
import monix.eval.Task
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Success

object TaskStackOverflowTest extends TestSuite[Scheduler] {
  def setup(): Scheduler = Scheduler.global
  def tearDown(env: Scheduler): Unit = ()

  test("flatMap(self) should not trigger stack overflow") { implicit s =>
    // note, this can trigger stack overflows
    def sum(n: Int, acc: Long = 0): Task[Long] = {
      if (n == 0) Task.now(acc) else
        Task.now(n).flatMap(x => sum(x-1, acc + x))
    }

    val nr = 2000000
    val f = sum(nr).runAsync
    Await.ready(f, 30.seconds)

    assertEquals(f.value.get, Success(nr.toLong / 2 * (nr.toLong + 1)))
  }
}
