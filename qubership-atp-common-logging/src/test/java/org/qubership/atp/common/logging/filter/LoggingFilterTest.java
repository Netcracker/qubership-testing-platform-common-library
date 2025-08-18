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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.qubership.atp.common.logging.config.LoggingProperties;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.ContentCachingResponseWrapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("checkstyle:MagicNumber")
@RunWith(SpringRunner.class)
public class LoggingFilterTest {

    /**
     * Request object.
     */
    private HttpServletRequest request;

    /**
     * Response object.
     */
    private HttpServletResponse response;

    /**
     * Chain of filters.
     */
    private FilterChain filterChain;

    /**
     * LoggingFilter object under testing.
     */
    private LoggingFilter loggingFilter;

    /**
     * LoggingProperties bean.
     */
    private LoggingProperties properties;

    /**
     * Logging Appender.
     */
    private ListAppender<ILoggingEvent> listAppender;

    /**
     * Input Stream for some tests (for requests and/or responses).
     */
    private ServletInputStream inputStream;

    /**
     * Expected log message of request containing test body.
     */
    private static final String FOUR_BYTE_TEST_BODY_LOG = "HTTP REQUEST DATA:\n"
            + "METHOD: null\n"
            + "URL: null\n"
            + "BODY: test\n"
            + "END HTTP (4-byte body)";

    /**
     * Expected log message of successful response with empty body.
     */
    private static final String EMPTY_BODY_LOG = "HTTP RESPONSE DATA:\n"
            + "HTTP STATUS: 200 OK\n"
            + "BODY: \n"
            + "END HTTP (0-byte body)";

    /**
     * Expected log message of successful response with not allowed body logging.
     */
    private static final String LOGGING_NOT_ALLOWED_STATUS_OK_LOG = "HTTP RESPONSE DATA:\n"
            + "HTTP STATUS: 200 OK\n"
            + "BODY: Body content logging is not allowed for current content type\n"
            + "END HTTP (60-byte body)";

    /**
     * Expected log message of request with not allowed body logging.
     */
    private static final String LOGGING_NOT_ALLOWED_LOG = "HTTP REQUEST DATA:\n"
            + "METHOD: null\n"
            + "URL: null\n"
            + "BODY: Body content logging is not allowed for current content type\n"
            + "END HTTP (60-byte body)";

    /**
     * Init objects and logging before tests.
     */
    @Before
    public void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);
        properties = Mockito.mock(LoggingProperties.class);
        loggingFilter = new LoggingFilter(properties);
        listAppender = new ListAppender<>();
        listAppender.start();
        ((Logger) LoggerFactory.getLogger(LoggingFilter.class)).addAppender(listAppender);
        inputStream = new DelegatingServletInputStream(new ByteArrayInputStream("test".getBytes()));
    }

    /**
     * Test of logging in case neither request nor response has Content-Type header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenReqAndRespWithoutHeaderContentType() throws ServletException, IOException {
        configureCommonResponseWhen(response, 200, null, null);
        loggingFilter.doFilter(request, response, filterChain);
        checkLogs("BODY: ", LOGGING_NOT_ALLOWED_LOG, LOGGING_NOT_ALLOWED_STATUS_OK_LOG);
    }

    /**
     * Test of logging in case request has body and valid Content-Type header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenRequestWithContentTypeAndBody() throws ServletException, IOException {
        configureCommonRequestWhen(request, "application/json", inputStream);
        configureCommonResponseWhen(response, 200, null, null);
        loggingFilter.doFilter(request, response, filterChain);
        checkLogs(FOUR_BYTE_TEST_BODY_LOG, LOGGING_NOT_ALLOWED_STATUS_OK_LOG);
    }

    /**
     * Test of body ignoring in case response has Content-Disposition header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenContentDispositionHeaderExistThenBodyIgnored() throws ServletException, IOException {
        configureCommonRequestWhen(request, "application/json", inputStream);
        configureCommonResponseWhen(response, 200, null, "Some_file_a5522837772.csv");
        loggingFilter.doFilter(request, response, filterChain);
        checkLogs(FOUR_BYTE_TEST_BODY_LOG,
                "HTTP RESPONSE DATA:\n"
                + "HTTP STATUS: 200 OK\n"
                + "BODY: Body content logging is not allowed for current Content-Disposition\n"
                + "END HTTP (67-byte body)");
    }

    /**
     * Test of logging in case response has body and valid Content-Type header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenResponseWithContentTypeAndBody() throws ServletException, IOException {
        configureCommonResponseWhen(response, 200, "application/json", null);
        loggingFilter.doFilter(request, response, filterChain);
        checkLogs(LOGGING_NOT_ALLOWED_LOG, EMPTY_BODY_LOG);
    }

    /**
     * Test of logging in case both request and response have valid Content-Type header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenRequestResponseWithContentType() throws ServletException, IOException {
        configureCommonRequestWhen(request, "application/json", inputStream);
        ContentCachingResponseWrapper response = Mockito.mock(ContentCachingResponseWrapper.class);
        Mockito.when(response.getContentInputStream()).thenReturn(inputStream);
        configureCommonResponseWhen(response, 200, "application/json", null);
        loggingFilter.doFilter(request, response, filterChain);
        checkLogs(FOUR_BYTE_TEST_BODY_LOG, EMPTY_BODY_LOG);
    }

    private void checkLogs(final String...args) {
        for(String arg : args) {
            assertTrue(listAppender.list.stream().anyMatch(m -> m.getFormattedMessage().contains(arg)));
        }
    }

    private void configureCommonRequestWhen(final HttpServletRequest request,
                                            final String contentType,
                                            final ServletInputStream stream) throws IOException {
        if (contentType != null) {
            Mockito.when(request.getHeader("Content-Type")).thenReturn(contentType);
        }
        if (stream != null) {
            Mockito.when(request.getInputStream()).thenReturn(stream);
        }
    }

    private void configureCommonResponseWhen(final HttpServletResponse response,
                                             final Integer status,
                                             final String contentType,
                                             final String contentDisposition) {
        if (status != null) {
            Mockito.when(response.getStatus()).thenReturn(status);
        }
        if (contentType != null) {
            Mockito.when(response.getHeader("Content-Type")).thenReturn(contentType);
        }
        if (contentDisposition != null) {
            Mockito.when(response.getHeader("Content-Disposition")).thenReturn(contentDisposition);
        }
    }

}
