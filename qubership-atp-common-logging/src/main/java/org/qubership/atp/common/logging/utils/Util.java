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

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.qubership.atp.common.logging.utils.RegexUtil.removeByKeyRegexPatterns;

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

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import org.qubership.atp.common.logging.adapter.AtpHttpResponse;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {

    /**
     * Error message for logging is not allowed for content-type.
     */
    private static final String MESSAGE = "Body content logging is not allowed for current content type";

    /**
     * Removes entries from {@link HttpHeaders} if the key matches any of regex patterns.
     *
     * @param headers headers to be filtered
     * @param patterns list of defined regex patterns
     * @return filtered map of headers.
     */
    public static Map<String, Collection<String>> filterHeaders(final HttpHeaders headers,
                                                                final List<Pattern> patterns) {
        Map<String, Collection<String>> headersMap =
                headers.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return removeByKeyRegexPatterns(headersMap, patterns);
    }

    /**
     * Returns {@link HttpHeaders} from map of headers.
     *
     * @param map Map of headers
     * @return {@link HttpHeaders} object.
     */
    public static HttpHeaders getHttpHeaders(final Map<String, Collection<String>> map) {
        Map<String, List<String>> headers = new HashMap<>();
        map.forEach((key, value) -> headers.put(key, new ArrayList<>(value)));
        MultiValueMap<String, String> multiValueMap = CollectionUtils.toMultiValueMap(headers);
        return new HttpHeaders(multiValueMap);
    }

    /**
     * Returns {@link List} of {@link String} with request data.
     *
     * @param request HttpServletRequest to be logged
     * @param isLoggedHeaders Flag if headers were logged
     * @param ignoreHeadersPattern List of patterns to ignore headers
     * @return List of String log messages.
     */
    public static List<String> getLogRequestData(final AtpHttpRequest request,
                                                 final Boolean isLoggedHeaders,
                                                 final List<Pattern> ignoreHeadersPattern) {
        byte[] body = request.getBody();
        return composeLogs(isLoggedHeaders,
                ignoreHeadersPattern,
                request.getMethod(),
                request.getUri(),
                isLoggedHeaders ? request.getHeaders() : null,
                Objects.nonNull(body) ? new String(body, defaultCharset()) : StringUtils.EMPTY);
    }

    /**
     * Returns {@link List} of {@link String} with Http request data.
     *
     * @param request HttpServletRequest to be logged
     * @param isLoggedHeaders Flag if headers were logged
     * @param ignoreHeadersPattern List of patterns to ignore headers
     * @return List of String log messages.
     */
    public static List<String> getLogHttpServletRequestData(final HttpServletRequest request,
                                                            final Boolean isLoggedHeaders,
                                                            final List<Pattern> ignoreHeadersPattern) {
        return composeLogs(isLoggedHeaders,
                ignoreHeadersPattern,
                request.getMethod(),
                request.getRequestURI(),
                isLoggedHeaders ? getHeaders(request) : null,
                MESSAGE);
    }

    private static List<String> composeLogs(final Boolean isLoggedHeaders,
                                            final List<Pattern> ignoreHeadersPattern,
                                            final String requestMethod,
                                            final String requestUri,
                                            final HttpHeaders requestHeaders,
                                            final String bodyText) {
        List<String> logs = new ArrayList<>();
        logs.add("HTTP REQUEST DATA:");
        logs.add(format("METHOD: %s", requestMethod));
        logs.add(format("URL: %s", requestUri));
        if (isLoggedHeaders) {
            logs.add(format("HEADERS: %s", filterHeaders(requestHeaders, ignoreHeadersPattern)));
        }
        logs.add(format("BODY: %s", bodyText));
        logs.add(format("END HTTP (%s-byte body)", bodyText.length()));
        return logs;
    }

    /**
     * Returns {@link HttpServletRequest} of {@link HttpHeaders} with http request data.
     *
     * @param request HttpServletRequest to get headers from
     * @return HttpHeaders object.
     */
    public static HttpHeaders getHeaders(final HttpServletRequest request) {
        Set<String> headerNames = new HashSet<>(Collections.list(request.getHeaderNames()));
        Map<String, Collection<String>> headers = headerNames.stream().collect(
                Collectors.toMap(Function.identity(), header -> Collections.list(request.getHeaders(header))));
        return Util.getHttpHeaders(headers);
    }

    /**
     * Returns {@link HttpServletResponse} of {@link HttpHeaders} with http response data.
     *
     * @param response HttpServletResponse to get headers from
     * @return HttpHeaders object.
     */
    public static HttpHeaders getHeaders(final HttpServletResponse response) {
        Set<String> headerNames = new HashSet<>(response.getHeaderNames());
        Map<String, Collection<String>> headers = headerNames.stream().collect(
                Collectors.toMap(Function.identity(), response::getHeaders));
        return Util.getHttpHeaders(headers);
    }

    /**
     * Returns {@link List} of {@link String} with response data.
     *
     * @param response AtpHttpResponse object to be logged
     * @param body String body of response
     * @param isLoggedHeaders Flag if headers were logged
     * @param ignorePattern List of patterns to ignore headers
     * @return List of String log messages
     * @throws IOException in case IO errors occurred.
     */
    public static List<String> getLogResponseData(final AtpHttpResponse response,
                                                  final String body,
                                                  final Boolean isLoggedHeaders,
                                                  final List<Pattern> ignorePattern) throws IOException {
        return getLogResponseData(
                response.getHeaders(), response.getStatusCode(), body, isLoggedHeaders, ignorePattern);
    }

    /**
     * Returns {@link List} of {@link String} with response data.
     *
     * @param headers HttpHeaders of response
     * @param status HttpStatus of response
     * @param body String body of response
     * @param isLoggedHeaders Flag if headers were logged
     * @param ignoreHeadersPattern List of patterns to ignore headers
     * @return List of String log messages.
     */
    public static List<String> getLogResponseData(final HttpHeaders headers,
                                                  final HttpStatus status,
                                                  final String body,
                                                  final Boolean isLoggedHeaders,
                                                  final List<Pattern> ignoreHeadersPattern) {
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
     * Logs message on Debug Level.
     *
     * @param logger Logger to be used
     * @param message String message to be logged.
     */
    public static void logMessage(final Logger logger, final String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }
}
