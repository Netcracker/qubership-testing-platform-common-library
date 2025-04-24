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

import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexUtil {

    /**
     * Removes entries from targetMap if the key matches any of regex patterns.
     * @param targetMap target collection to be filtered.
     * @param patterns list of defined regex patterns.
     * @return filtered targetMap.
     */
    public static Map<String, Collection<String>> removeByKeyRegexPatterns(Map<String, Collection<String>> targetMap,
                                                                           List<Pattern> patterns) {
        return targetMap.entrySet()
            .stream()
            .filter(entry -> matchKey(entry.getKey(), patterns))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Removes entries from targetCollection if the element matches any of regex patterns.
     * @param targetCollection target collection to be filtered.
     * @param patterns list of defined regex patterns.
     * @return filtered targetCollection.
     */
    public static Collection<String> removeByRegexPatterns(Collection<String> targetCollection,
                                                           List<Pattern> patterns) {
        return targetCollection
            .stream()
            .filter(key -> matchKey(key, patterns))
            .collect(Collectors.toList());
    }

    /**
     * Analyses that input string matches any of regex patterns.
     * @param string string for analyse
     * @param patterns list of defined regex patterns.
     * @return true if string matches any of patterns, otherwise return false.
     */
    public static boolean matchKey(String string, List<Pattern> patterns) {
        return patterns
            .stream()
            .noneMatch(s -> matchKey(string, s));
    }

    private static boolean matchKey(String string, Pattern pattern) {
        Matcher matcher = pattern.matcher(string);
        return matcher.find();
    }
}
