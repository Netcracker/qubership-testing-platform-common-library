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

import static org.qubership.atp.common.logging.utils.RegexUtil.removeByKeyRegexPatterns;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import org.qubership.atp.common.logging.adapter.AtpHttpResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {

    private static String MESSAGE = "Body content logging is not allowed for current content type";

    /**
     * Removes entries from {@link HttpHeaders} if the key matches any of regex patterns.
     * @param headers  headers to be filtered.
     * @param patterns list of defined regex patterns.
     * @return filtered map of headers.
     */
    public static Map<String, Collection<String>> filterHeaders(HttpHeaders headers, List<Pattern> patterns) {
        Map<String, Collection<String>> headersMap =
                headers.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return removeByKeyRegexPatterns(headersMap, patterns);
    }

    /**
     * Returns {@link HttpHeaders} from map of headers.
     * @param map map of headers
     * @return {@link HttpHeaders}.
     */
    public static HttpHeaders getHttpHeaders(Map<String, Collection<String>> map) {
        Map<String, List<String>> headers = new HashMap<>();
        map.forEach((key, value) -> headers.put(key, new ArrayList<>(value)));
        MultiValueMap<String, String> multiValueMap = CollectionUtils.toMultiValueMap(headers);
        return new HttpHeaders(multiValueMap);
    }

    /**
     * Returns {@link List} of {@link String} with request data.
     */
    public static List<String> getLogRequestData(AtpHttpRequest request,
                                                 Boolean isLoggedHeaders,
                                                 List<Pattern> ignoreHeadersPattern) {
        List<String> logs = new ArrayList<>();
        logs.add("HTTP REQUEST DATA:");
        logs.add(format("METHOD: %s", request.getMethod()));
        logs.add(format("URL: %s", request.getUri()));
        if (isLoggedHeaders) {
            logs.add(format("HEADERS: %s", filterHeaders(request.getHeaders(), ignoreHeadersPattern)));
        }
        byte[] body = request.getBody();
        String bodyText = Objects.nonNull(body) ? new String(body, defaultCharset()) : StringUtils.EMPTY;
        logs.add(format("BODY: %s", bodyText));
        logs.add(format("END HTTP (%s-byte body)", bodyText.length()));
        return logs;
    }

    /**
     * Returns {@link List} of {@link String} with Http request data.
     */

    public static List<String> getLogHttpServletRequestData(HttpServletRequest request,
                                                            Boolean isLoggedHeaders,
                                                            List<Pattern> ignoreHeadersPattern) {
        List<String> logs = new ArrayList<>();
        logs.add("HTTP REQUEST DATA:");
        logs.add(format("METHOD: %s", request.getMethod()));
        logs.add(format("URL: %s", request.getRequestURI()));
        if (isLoggedHeaders) {
            logs.add(format("HEADERS: %s", filterHeaders(getHeaders(request), ignoreHeadersPattern)));
        }

        String bodyText = MESSAGE;
        logs.add(format("BODY: %s", bodyText));
        logs.add(format("END HTTP (%s-byte body)", bodyText.length()));
        return logs;
    }

    /**
     * Returns {@link HttpServletRequest} of {@link HttpHeaders} with http request data.
     */
    public static HttpHeaders getHeaders(HttpServletRequest request) {
        Set<String> headerNames = new HashSet<>(Collections.list(request.getHeaderNames()));
        Map<String, Collection<String>> headers = headerNames.stream().collect(
                Collectors.toMap(Function.identity(), header -> Collections.list(request.getHeaders(header))));
        return Util.getHttpHeaders(headers);
    }

    /**
     * Returns {@link HttpServletResponse} of {@link HttpHeaders} with http response data.
     */
    public static HttpHeaders getHeaders(HttpServletResponse response) {
        Set<String> headerNames = new HashSet<>(response.getHeaderNames());
        Map<String, Collection<String>> headers = headerNames.stream().collect(
                Collectors.toMap(Function.identity(), header -> response.getHeaders(header)));
        return Util.getHttpHeaders(headers);
    }

    /**
     * Returns {@link List} of {@link String} with response data.
     */
    public static List<String> getLogResponseData(AtpHttpResponse response,
                                                  String body,
                                                  Boolean isLoggedHeaders,
                                                  List<Pattern> ignorePattern) throws IOException {
        return getLogResponseData(
                response.getHeaders(), response.getStatusCode(), body, isLoggedHeaders, ignorePattern);
    }

    /**
     * Returns {@link List} of {@link String} with response data.
     */
    public static List<String> getLogResponseData(HttpHeaders headers,
                                                  HttpStatus status,
                                                  String body,
                                                  Boolean isLoggedHeaders,
                                                  List<Pattern> ignoreHeadersPattern) {
        List<String> logs = new ArrayList<>();
        logs.add("HTTP RESPONSE DATA:");
        logs.add(format("HTTP STATUS: %s %s", status.value(), status.getReasonPhrase()));
        if (isLoggedHeaders) {
            logs.add(format("HEADERS: %s", filterHeaders(headers, ignoreHeadersPattern)));
        }
        logs.add(format("BODY: %s", body));
        logs.add(format("END HTTP (%s-byte body)", body.length()));
        return logs;
    }

    /**
     * Logs message.
     */
    public static void logMessage(Logger logger, String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }
}