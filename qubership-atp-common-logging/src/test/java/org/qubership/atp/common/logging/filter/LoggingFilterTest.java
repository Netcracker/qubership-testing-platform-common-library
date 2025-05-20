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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);

        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains("BODY: ")));
        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(LOGGING_NOT_ALLOWED_LOG)));
        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(LOGGING_NOT_ALLOWED_STATUS_OK_LOG)));
    }

    /**
     * Test of logging in case request has body and valid Content-Type header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenRequestWithContentTypeAndBody() throws ServletException, IOException {
        Mockito.when(request.getInputStream()).thenReturn(inputStream);
        Mockito.when(request.getHeader("Content-Type")).thenReturn("application/json");

        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);

        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(FOUR_BYTE_TEST_BODY_LOG)));
        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(LOGGING_NOT_ALLOWED_STATUS_OK_LOG)));
    }

    /**
     * Test of body ignoring in case response has Content-Disposition header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenContentDispositionHeaderExistThenBodyIgnored() throws ServletException, IOException {
        Mockito.when(request.getInputStream()).thenReturn(inputStream);
        Mockito.when(request.getHeader("Content-Type")).thenReturn("application/json");

        Mockito.when(response.getHeader("Content-Disposition"))
                .thenReturn("TDM_c8de91d01cad47dcb25714fd766c264a5522836723710187772.csv");
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);

        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(FOUR_BYTE_TEST_BODY_LOG)));
        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains("HTTP RESPONSE DATA:\n"
                        + "HTTP STATUS: 200 OK\n"
                        + "BODY: Body content logging is not allowed for current Content-Disposition\n"
                        + "END HTTP (67-byte body)")));
    }

    /**
     * Test of logging in case response has body and valid Content-Type header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenResponseWithContentTypeAndBody() throws ServletException, IOException {
        Mockito.when(response.getHeader("Content-Type")).thenReturn("application/json");
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);

        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(LOGGING_NOT_ALLOWED_LOG)));
        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(EMPTY_BODY_LOG)));
    }

    /**
     * Test of logging in case both request and response have valid Content-Type header.
     *
     * @throws ServletException in case servlet execution exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void whenRequestResponseWithContentType() throws ServletException, IOException {
        Mockito.when(request.getInputStream()).thenReturn(inputStream);
        Mockito.when(request.getHeader("Content-Type")).thenReturn("application/json");

        ContentCachingResponseWrapper response = Mockito.mock(ContentCachingResponseWrapper.class);
        Mockito.when(response.getContentInputStream()).thenReturn(inputStream);
        Mockito.when(response.getHeader("Content-Type")).thenReturn("application/json");
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);

        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(FOUR_BYTE_TEST_BODY_LOG)));
        assertTrue(listAppender.list.stream().anyMatch(m ->
                m.getFormattedMessage().contains(EMPTY_BODY_LOG)));
    }
}
