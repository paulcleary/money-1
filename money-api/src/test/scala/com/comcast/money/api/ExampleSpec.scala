/*
 * Copyright 2012-2015 Comcast Cable Communications Management, LLC
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

package com.comcast.money.api

import java.util.concurrent.Executors

import org.scalatest.{ Matchers, WordSpec }

class ExampleSpec extends WordSpec with Matchers {

  val executors = Executors.newFixedThreadPool(10)

  "The prototype core" should {
    "work" in {
      val handler = new DefaultSpanHandler()
      val monitor = new DefaultSpanMonitor(handler)
      val tracer = new DefaultSpanFactory(handler, monitor)

      val iWillTimeout = tracer.newSpan("timeMeOut")

      for (i <- 1 to 2) {
        executors.submit(
          new Runnable {
            override def run(): Unit = {
              val fooSpan = tracer.newSpan("foo")
              fooSpan.record(Note.of("iam", "foo"))

              for (j <- 1 to 2) {
                val barSpan = tracer.newSpan("bar")

                barSpan.record(Note.of("iam", "bar"))
                barSpan.stop(true)
              }

              fooSpan.stop(true)
            }
          }
        )
      }

      Thread.sleep(2000)
    }
  }
}
