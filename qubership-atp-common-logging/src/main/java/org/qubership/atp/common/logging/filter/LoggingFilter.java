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

package org.qubership.atp.common.logging.filter;

import static org.qubership.atp.common.logging.utils.Util.logMessage;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import org.qubership.atp.common.logging.adapter.filter.ContentCachingHttpRequest;
import org.qubership.atp.common.logging.adapter.filter.ContentCachingHttpResponse;
import org.qubership.atp.common.logging.config.LoggingProperties;
import org.qubership.atp.common.logging.utils.ContentType;
import org.qubership.atp.common.logging.utils.RegexUtil;
import org.qubership.atp.common.logging.utils.Util;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LoggingFilter implements Filter {

    /**
     * LoggingProperties object.
     */
    private final LoggingProperties loggingProperties;

    /**
     * "Content-Type" Constant.
     */
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * "Content-Disposition" Constant.
     */
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * Apply filter.
     *
     * @param request ServletRequest object
     * @param response ServletResponse object
     * @param chain Chain of filters
     * @throws IOException in case IO error occurred
     * @throws ServletException in case servlet errors.
     */
    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        List<Pattern> ignoreUriList = loggingProperties.getIgnoreUriListPatterns();
        if (request instanceof HttpServletRequest
                && RegexUtil.matchKey(((HttpServletRequest) request).getRequestURI(), ignoreUriList)
                && response instanceof HttpServletResponse) {
            doFilterWithContentCaching(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void doFilterWithContentCaching(final ServletRequest request,
                                            final ServletResponse response,
                                            final FilterChain chain) throws IOException, ServletException {
        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper((HttpServletResponse) response);
        if (ContentType.getContentType(((HttpServletRequest) request)
                .getHeader(HEADER_CONTENT_TYPE)).isLoggingAllowed()) {
            CommonHttpRequestWrapper wrappedRequest =
                    new CommonHttpRequestWrapper((HttpServletRequest) request);
            logRequest(wrappedRequest);
            chain.doFilter(wrappedRequest, wrappedResponse);
        } else {
            logRequest((HttpServletRequest) request);
            chain.doFilter(request, wrappedResponse);
        }

        try {
            this.logResponse(wrappedResponse);
        } finally {
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * Logs Request payload.
     *
     * @param request wrapped request with cached input stream.
     */
    private void logRequest(final HttpServletRequest request) {
        AtpHttpRequest wrappedRequest = null;
        if (request instanceof  CommonHttpRequestWrapper) {
            wrappedRequest = new ContentCachingHttpRequest((CommonHttpRequestWrapper) request);
        }

        Boolean isLoggedHeaders = loggingProperties.logControllerHeaders();
        List<Pattern> ignoreHeadersPattern = loggingProperties.getIgnoreControllerHeaders();

        final List<String> logs = request instanceof CommonHttpRequestWrapper
                ? Util.getLogRequestData(wrappedRequest, isLoggedHeaders, ignoreHeadersPattern)
                : Util.getLogHttpServletRequestData(request, isLoggedHeaders, ignoreHeadersPattern);

        logMessage(log, String.join(StringUtils.LF, logs));
    }

    /**
     * Logs Response payload.
     *
     * @param wrappedResponse wrapped response with cached output stream.
     */
    private void logResponse(final ContentCachingResponseWrapper wrappedResponse) throws IOException {

        String body;
        ContentCachingHttpResponse cacheResponse = new ContentCachingHttpResponse(wrappedResponse);

        if (Strings.isNotEmpty(wrappedResponse.getHeader(HEADER_CONTENT_DISPOSITION))) {
            body = "Body content logging is not allowed for current Content-Disposition";
        } else if (ContentType.getContentType(wrappedResponse.getHeader(HEADER_CONTENT_TYPE)).isLoggingAllowed()) {
            body = cacheResponse.getBody();
        } else {
            body = "Body content logging is not allowed for current content type";
        }

        Boolean isLoggedHeaders = loggingProperties.logControllerHeaders();
        List<Pattern> ignoreHeadersPattern = loggingProperties.getIgnoreControllerHeaders();

        final List<String> logs = Util.getLogResponseData(cacheResponse, body, isLoggedHeaders, ignoreHeadersPattern);
        logMessage(log, String.join(StringUtils.LF, logs));
    }
}
