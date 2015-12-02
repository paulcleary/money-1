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

public class Timer implements AutoCloseable {

    private final long startInstant;
    private final Span span;
    private final boolean propagate;
    private final String timerKey;

    public Timer(String timerKey, Span span, boolean propagate) {
        this.startInstant = System.nanoTime();
        this.timerKey = timerKey;
        this.span = span;
        this.propagate = propagate;
    }

    @Override
    public void close() throws Exception {
        long duration = System.nanoTime() - startInstant;
        span.record(Note.of(timerKey, duration));
    }
}
