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

public class Note<T> {

    private final String name;
    private final T value;
    private final Long timestamp;
    private final boolean propagated; // indicates that this note should be sent to child spans if so requested by user

    private Note(String name, T value) {
        this(name, value, System.currentTimeMillis(), false);
    }

    private Note(String name, T value, boolean propagated) {
        this(name, value, System.currentTimeMillis(), propagated);
    }

    private Note(String name, T value, Long timestamp, boolean propagated) {
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
        this.propagated = propagated;
    }

    public String name() {
        return name;
    }

    public T value() {
        return value;
    }

    public Long timestamp() {
        return timestamp;
    }

    public boolean isPropagated() {
        return propagated;
    }

    public static Note<String> of(String name, String value) {
        return new Note<String>(name, value);
    }

    public static Note<Long> of(String name, long value) {
        return new Note<Long>(name, value);
    }

    public static Note<Boolean> of(String name, boolean value) {
        return new Note<Boolean>(name, value);
    }

    public static Note<Double> of(String name, double value) {
        return new Note<Double>(name, value);
    }

    public static Note<String> of(String name, String value, boolean propagated) {
        return new Note<String>(name, value, propagated);
    }

    public static Note<Long> of(String name, Long value, boolean propagated) {
        return new Note<Long>(name, value, propagated);
    }

    public static Note<Boolean> of(String name, boolean value, boolean propagated) {
        return new Note<Boolean>(name, value, propagated);
    }

    public static Note<Double> of(String name, double value, boolean propagated) {
        return new Note<Double>(name, value, propagated);
    }
}
