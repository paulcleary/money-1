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

import java.lang
import java.lang.{ Double, Long }

import scala.collection._
import scala.collection.JavaConversions._

// obviously not threadsafe and very mutable, basing this kinda on what I did for the java-only core
class DefaultSpan(
    val id: SpanId,
    val spanName: String,
    spanMonitor: SpanMonitor,
    notes: mutable.Map[String, Note[_]] = new mutable.HashMap[String, Note[_]],
    propagate: Boolean = false
) extends Span {

  // TODO: yea, hacky, just prototyping though
  private var stopTime: Long = _
  private var stopInstant: Long = _
  private var startedTime: Long = _
  private var startInstant: Long = _
  private var success: Boolean = true

  override def startTime(): Long = startedTime

  override def start(): Unit = {
    startedTime = System.currentTimeMillis
    startInstant = System.nanoTime

    // Start watching me
    spanMonitor.watch(this)

    // Push me onto thread local
    SpanLocal.push(this)
  }

  override def childSpan(childName: String, propagate: Boolean): Span = {
    if (propagate)
      new DefaultSpan(
        id.newChild(),
        childName,
        spanMonitor,
        notes.filter(propagatedNotes),
        propagate
      )
    else
      new DefaultSpan(
        id.newChild(),
        childName,
        spanMonitor
      )
  }

  override def stop(): Unit = stop(true)

  override def stop(result: Boolean): Unit = {
    stopTime = System.currentTimeMillis
    stopInstant = System.nanoTime
    success = result

    // stop watching me
    spanMonitor.unwatch(this)

    // Remove this span from span local if it is the one in scope
    // NOTE not sure if we are cleaning up thread local properly yet
    SpanLocal.current.foreach {
      current =>
        if (current.id == this.id)
          SpanLocal.pop()
    }
  }

  override def startTimer(timerKey: String): Timer =
    new Timer(timerKey, this, false)

  override def data(): SpanData =
    new SpanData(notes, startedTime, stopTime, success, id, spanName, stopInstant - startInstant)

  override def record(note: Note[_]): Unit = notes += note.getName -> note

  private def propagatedNotes: ((String, Note[_])) => Boolean = tuple => tuple._2.isPropagated
}
