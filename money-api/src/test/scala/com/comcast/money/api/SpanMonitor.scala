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

import java.util.concurrent.{ TimeUnit, Executors, LinkedBlockingQueue }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SpanMonitor {

  def watch(span: Span)

  def unwatch(span: Span)

  def inScope: Option[Span]
}

class DefaultSpanMonitor(spanHandler: SpanHandler) extends SpanMonitor {

  private val activeSpans = new LinkedBlockingQueue[Span]()
  private val closingSpans = new LinkedBlockingQueue[Span]()
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  // hardcoded setup
  private val timeout = 1000

  // hardcode to sweep every 100 millis
  scheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = sweep()
  }, 100L, 100L, TimeUnit.MILLISECONDS)

  override def inScope: Option[Span] = SpanLocal.current

  override def watch(span: Span): Unit =
    activeSpans.offer(span)

  override def unwatch(span: Span): Unit = {
    activeSpans.remove(span)
    closingSpans.offer(span)
  }

  private def isTimedOut(span: Span): Boolean = {
    val expireTime = span.startTime + timeout
    val timedOut = System.currentTimeMillis < expireTime
    println(s"\r\n checking if timed out for span")
    println(s"expireTime is $expireTime; now is ${System.currentTimeMillis}")
    println(s"isTimedOut = $timedOut")
    System.currentTimeMillis < expireTime
  }

  private def sweep(): Unit = {
    println(s"\r\n!!! SWEEPING ACTIVE COUNT = ${activeSpans.size}; CLOSING COUNT = ${closingSpans.size}")
    // first pass is to finalize all activeSpans that should be closed
    // we start at the top of the queue since those are the oldest, if they are closed
    // then we can remove them from the queue
    while (!closingSpans.isEmpty) {
      val closedSpan = closingSpans.poll()
      spanHandler.handle(closedSpan.data)
    }

    // because this is a LinkedBlockingQueue, we can assert that the queue is ordered FIFO,
    // so the oldest appear at the top of the queue...if the items at the top of the queue
    // are not expired, then it follows that those later down in the list are also not expired
    while (!activeSpans.isEmpty && isTimedOut(activeSpans.peek())) {
      println(s"\r\n\r\n!!! TIMED OUT SPAN!!!")
      val timedOutSpan = activeSpans.poll()

      // stopping the span will force an unwatch and he will get cleaned up next sweep
      // has to be in a future or we deadlock
      // TODO: we should probably record a timed out note as well
      Future(timedOutSpan.stop(false))
    }
  }
}
