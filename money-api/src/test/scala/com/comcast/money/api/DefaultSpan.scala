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

import java.lang.Long
import java.util

import scala.collection._
import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap

// obviously not threadsafe and very mutable, basing this kinda on what I did for the java-only core
case class DefaultSpan(
    id: SpanId,
    name: String,
    handler: SpanHandler,
    startTime: Long = null,
    startInstant: Long = null,
    endTime: Long = null,
    endInstant: Long = null,
    duration: Long = 0,
    success: java.lang.Boolean = true,
    recordedNotes: Map[String, Note[_]] = new HashMap[String, Note[_]](),
    timers: Map[String, Long] = new HashMap[String, Long](),
    propagate: Boolean = false
) extends Span {

  private val HEADER_FORMAT: String = "Span: [ span-id=%s ][ trace-id=%s ][ parent-id=%s ][ span-name=%s ][ " +
    "app-name=%s ][ start-time=%s ][ span-duration=%s ][ span-success=%s ]"
  private val NOTE_FORMAT: String = "[ %s=%s ]"
  private val NULL: String = "NULL"

  override def notes(): util.Map[String, Note[_]] = mapAsJavaMap(recordedNotes)

  override def start(): Span = {
    copy(
      startTime = System.currentTimeMillis(),
      startInstant = System.nanoTime()
    )
  }

  override def childSpan(childName: String, propagate: Boolean): Span = {
    if (propagate)
      new DefaultSpan(
        id = id.newChild(),
        name = childName,
        handler = handler,
        recordedNotes = recordedNotes.filter(propagatedNotes),
        propagate = propagate
      ).start()
    else
      new DefaultSpan(
        id = id.newChild(),
        name = childName,
        handler = handler
      ).start()
  }

  override def stop(): Span = stop(true)

  override def stop(result: Boolean): Span = {
    val stopped = copy(
      endTime = System.currentTimeMillis(),
      endInstant = System.nanoTime(),
      duration = System.nanoTime() - startInstant,
      success = result
    )
    handler.handle(stopped)
    stopped
  }

  override def startTimer(timerKey: String): Span =
    copy(
      timers = timers + (timerKey -> System.nanoTime())
    )

  override def stopTimer(timerKey: String): Span =
    timers
      .get(timerKey)
      .map(started => Note.of(timerKey, System.nanoTime() - started))
      .map(record)
      .getOrElse(this)

  override def record(note: Note[_]): Span =
    copy(
      recordedNotes = recordedNotes + (note.name -> note)
    )

  override def toString(): String = {
     val sb: StringBuilder = new StringBuilder
     sb.append(
       HEADER_FORMAT.format(
         id.selfId, id.traceId, id.parentId, name, "app",
         startTime, duration, success))
     if (recordedNotes != null) {
       for (note <- recordedNotes.values) {
         sb.append(NOTE_FORMAT.format(note.name, valueOrNull(note.value)))
       }
     }
     sb.toString
   }

  private def valueOrNull[T](value: T) = Option(value).getOrElse(NULL)

  private def propagatedNotes: ((String, Note[_])) => Boolean = tuple => tuple._2.isPropagated
}
