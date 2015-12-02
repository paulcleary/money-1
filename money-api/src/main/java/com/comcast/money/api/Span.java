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

package com.comcast.money.api;

/**
 * A Span is a container that represents a single invocation of a traced service / operation.
 */
public interface Span {

    /**
     * Signals the span that it has started
     */
    void start();

    /**
     * Stops the span asserts a successful result
     */
    void stop();

    /**
     * Ends a span, moving it to a Stopped state
     * @param result The result of the span (success or failure)
     */
    void stop(boolean result);

    /**
     * Records a given note onto the span.  If the note was already present, it will be overwritten
     * @param note
     */
    void record(Note<?> note);

    /**
     * Starts a timer
     *
     * @param timerKey The name of the timer, this will be used for the name of the note that is emitted
     */
    Timer startTimer(String timerKey);

    /**
     * @return The SpanId for this span
     */
    SpanId id();

    /**
     * @return The SpanData that is collected so far for this span.  This should be a COPY of the data and
     * as such is immutable.  The data does not reflect updates since it was retrieved.  To see the current
     * data, call this method again to get the latest span data.
     */
    SpanData data();

    Long startTime();

    /**
     * Creates a new child from this span
     *
     * @param childName The name of the child to create
     * @param propagate True if the propagated notes from this span should be passed to the child
     * @return A new Span that is a child of this span
     */
    Span childSpan(String childName, boolean propagate);
}
