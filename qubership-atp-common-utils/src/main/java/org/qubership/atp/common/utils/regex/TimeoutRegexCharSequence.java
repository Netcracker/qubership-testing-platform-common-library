/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.common.utils.regex;

public class TimeoutRegexCharSequence implements CharSequence {

    /**
     * CharSequence object.
     */
    private final CharSequence inner;

    /**
     * Timeout value in millis.
     */
    private final int timeoutMillis;

    /**
     * Timeout value in seconds.
     */
    private final long timeoutTime;

    /**
     * Constructor for TimeoutRegexCharSequence.
     *
     * @param inner regexp pattern
     * @param timeout timeout (seconds) for regexp processing.
     */
    public TimeoutRegexCharSequence(final CharSequence inner, final int timeout) {
        super();
        this.inner = inner;
        timeoutMillis = timeout * 1000;
        timeoutTime = System.currentTimeMillis() + timeoutMillis;
    }

    /**
     * Method for regexp processing to char with timeout check.
     *
     * @param index of char
     * @return char at specified index; but if timeoutTime is over, TimeoutRegexException is thrown instead.
     */
    public char charAt(final int index) {
        if (System.currentTimeMillis() > timeoutTime) {
            String message = String
                    .format("Timeout occurred after %s ms while processing regular expression %s.", timeoutMillis,
                            inner);
            throw new TimeoutRegexException(message);
        }
        return inner.charAt(index);
    }

    /**
     * Get Length of inner CharSequence.
     *
     * @return int length of inner CharSequence.
     */
    public int length() {
        return inner.length();
    }

    /**
     * Get SubSequence of inner CharSequence.
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return new TimeoutRegexCharSequence for inner.subSequence(start, end) and timeoutMillis.
     */
    public CharSequence subSequence(final int start, final int end) {
        return new TimeoutRegexCharSequence(inner.subSequence(start, end), timeoutMillis);
    }

    /**
     * Make String representation.
     *
     * @return String representation of the object; inner.toString() currently.
     */
    @Override
    public String toString() {
        return inner.toString();
    }
}
