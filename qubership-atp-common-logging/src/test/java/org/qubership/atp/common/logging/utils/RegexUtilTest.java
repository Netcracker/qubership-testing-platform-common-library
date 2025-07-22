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

package org.qubership.atp.common.logging.utils;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.qubership.atp.common.logging.utils.RegexUtil.matchKey;
import static org.qubership.atp.common.logging.utils.RegexUtil.removeByKeyRegexPatterns;
import static org.qubership.atp.common.logging.utils.RegexUtil.removeByRegexPatterns;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class RegexUtilTest {

    /**
     * Constant for Bearer token prefix.
     */
    private static final String BEARER_HEADER = "Bearer";

    /**
     * Constant for X-Request-Id header name.
     */
    private static final String X_REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * Constant for Zipkin-Trace-Id header name.
     */
    private static final String ZIPKIN_TRACE_ID_HEADER = "Zipkin-Trace-Id";

    /**
     * Collection of Header names to collect.
     */
    private Collection<String> targetCollection;

    /**
     * Collection of expected header values.
     */
    private Collection<String> expectedResult;

    /**
     * List of patterns to check/extract header values.
     */
    private List<Pattern> patterns;

    /**
     * Collection of headers.
     */
    private Map<String, Collection<String>> targetMap;

    /**
     * Init environment before tests.
     */
    @Before
    public void setUp() {
        targetCollection = unmodifiableList(asList(BEARER_HEADER, X_REQUEST_ID_HEADER, ZIPKIN_TRACE_ID_HEADER));
        targetMap = targetCollection.stream().collect(toMap(String::toString, Collections::singletonList));
        expectedResult = singletonList(ZIPKIN_TRACE_ID_HEADER);
        patterns = unmodifiableList(asList(Pattern.compile(BEARER_HEADER), Pattern.compile("\\D*Request\\D*")));
    }

    /**
     * Test of matching any pattern.
     */
    @Test
    public void testMatchKeyShouldReturnFalseWhenInputStringMatchesAnyPattern() {
        boolean matches = matchKey(BEARER_HEADER, patterns);
        assertFalse(matches);
    }

    /**
     * Test of not matching any pattern.
     */
    @Test
    public void testMatchKeyShouldReturnTrueWhenInputStringNotMatchesAnyPattern() {
        boolean matches = matchKey(ZIPKIN_TRACE_ID_HEADER, patterns);
        assertTrue(matches);
    }

    /**
     * Test of matching in case null String.
     */
    @Test(expected = NullPointerException.class)
    public void testMatchKeyShouldThrowAnExceptionWhenInputStringIsNull() {
        matchKey(null, patterns);
    }

    /**
     * Test of matching in case patterns are null.
     */
    @Test(expected = NullPointerException.class)
    public void testMatchKeyShouldThrowAnExceptionWhenPatternsAreNull() {
        matchKey(BEARER_HEADER, null);
    }

    /**
     * Test of removing headers by patterns.
     */
    @Test
    public void testRemoveByRegexPatternsShouldRemoveHeadersWhenPatternsAreNotEmpty() {
        Collection<String> result = removeByRegexPatterns(targetCollection, patterns);
        assertThat(result, equalTo(expectedResult));
    }

    /**
     * Test of removing headers by patterns in case no patterns.
     */
    @Test
    public void testRemoveByRegexPatternsShouldExistAllHeadersWhenPatternsAreEmpty() {
        Collection<String> result = removeByRegexPatterns(targetCollection, newArrayList());
        assertThat(result, equalTo(targetCollection));
    }

    /**
     * Test of removing header names by patterns.
     */
    @Test
    public void testRemoveByKeyRegexPatternsShouldRemoveHeadersWhenPatternsAreNotEmpty() {
        Map<String, Collection<String>> result = removeByKeyRegexPatterns(targetMap, patterns);
        assertThat(newArrayList(result.keySet()), equalTo(expectedResult));
    }

    /**
     * Test of removing header names by patterns in case no patterns.
     */
    @Test
    public void testRemoveByKeyRegexPatternsShouldExistAllHeadersWhenPatternsAreEmpty() {
        Map<String, Collection<String>> result = removeByKeyRegexPatterns(targetMap, newArrayList());
        assertThat(result, equalTo(targetMap));
    }
}
