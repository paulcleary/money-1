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

class DefaultSpanFactory(spanHandler: SpanHandler, spanMonitor: SpanMonitor) extends SpanFactory {

  override def newSpan(spanName: String): Span = newSpan(spanName, false)

  // This is hacky, don't know how yet to resolve the API to support one implementation
  // working with ThreadLocal and a different implementation not working with ThreadLocal
  // As you can see, right now we don't have
  override def newSpan(spanName: String, propagate: Boolean): Span = {
    val span = spanMonitor.inScope.map(_.childSpan(spanName, propagate)).getOrElse {
      new DefaultSpan(
        new SpanId(),
        spanName,
        spanMonitor,
        propagate = propagate
      )
    }
    span.start()
    span
  }
}
