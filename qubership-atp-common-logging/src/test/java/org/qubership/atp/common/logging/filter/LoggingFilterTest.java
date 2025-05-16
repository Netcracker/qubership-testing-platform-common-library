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

@RunWith(SpringRunner.class)
public class LoggingFilterTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private LoggingFilter loggingFilter;
    private LoggingProperties properties;
    private ListAppender<ILoggingEvent> listAppender;

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
    }

    @Test
    public void whenReqAndRespWithoutHeaderContentType () throws ServletException, IOException {
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);
        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("BODY: ")));

        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP REQUEST DATA:\n"
                        + "METHOD: null\n"
                        + "URL: null\n"
                        + "BODY: Body content logging is not allowed for current content type\n"
                        + "END HTTP (60-byte body)")));
        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP RESPONSE DATA:\n"
                        + "HTTP STATUS: 200 OK\n"
                        + "BODY: Body content logging is not allowed for current content type\n"
                        + "END HTTP (60-byte body)")));
    }

    @Test
    public void whenRequestWithContentTypeAndBody() throws ServletException, IOException {
        ServletInputStream is= new DelegatingServletInputStream(new ByteArrayInputStream("test".getBytes()));
        Mockito.when(request.getInputStream()).thenReturn(is);
        Mockito.when(request.getHeader("Content-Type")).thenReturn("application/json");
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);

        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP REQUEST DATA:\n"
                        + "METHOD: null\n"
                        + "URL: null\n"
                        + "BODY: test\n"
                        + "END HTTP (4-byte body)")));

        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP RESPONSE DATA:\n"
                        + "HTTP STATUS: 200 OK\n"
                        + "BODY: Body content logging is not allowed for current content type\n"
                        + "END HTTP (60-byte body)")));
    }

    @Test
    public void responseFilter_contentDispositionHeaderExist_bodyIgnored() throws ServletException, IOException {
        ServletInputStream is = new DelegatingServletInputStream(new ByteArrayInputStream("test".getBytes()));
        Mockito.when(request.getInputStream()).thenReturn(is);
        Mockito.when(request.getHeader("Content-Type")).thenReturn("application/json");
        Mockito.when(response.getHeader("Content-Disposition"))
                .thenReturn("TDM_c8de91d01cad47dcb25714fd766c264a5522836723710187772.csv");
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);

        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP REQUEST DATA:\n"
                        + "METHOD: null\n"
                        + "URL: null\n"
                        + "BODY: test\n"
                        + "END HTTP (4-byte body)")));

        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP RESPONSE DATA:\n"
                        + "HTTP STATUS: 200 OK\n"
                        + "BODY: Body content logging is not allowed for current Content-Disposition\n"
                        + "END HTTP (67-byte body)")));
    }

    @Test
    public void whenResponseWithContentTypeAndBody() throws ServletException, IOException {
        Mockito.when(response.getHeader("Content-Type")).thenReturn("application/json");
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);
        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP REQUEST DATA:\n"
                        + "METHOD: null\n"
                        + "URL: null\n"
                        + "BODY: Body content logging is not allowed for current content type\n"
                        + "END HTTP (60-byte body)")));

        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP RESPONSE DATA:\n"
                        + "HTTP STATUS: 200 OK\n"
                        + "BODY: \n"
                        + "END HTTP (0-byte body)")));
    }

    @Test
    public void whenRequestResponseWithContentType() throws ServletException, IOException {
        ServletInputStream is= new DelegatingServletInputStream(new ByteArrayInputStream("test".getBytes()));
        ContentCachingResponseWrapper response = Mockito.mock(ContentCachingResponseWrapper.class);
        Mockito.when(request.getInputStream()).thenReturn(is);
        Mockito.when(response.getContentInputStream()).thenReturn(is);
        Mockito.when(request.getHeader("Content-Type")).thenReturn("application/json");
        Mockito.when(response.getHeader("Content-Type")).thenReturn("application/json");
        Mockito.when(response.getStatus()).thenReturn(200);
        loggingFilter.doFilter(request, response, filterChain);

        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP REQUEST DATA:\n"
                        + "METHOD: null\n"
                        + "URL: null\n"
                        + "BODY: test\n"
                        + "END HTTP (4-byte body)")));
        assertTrue(listAppender.list.stream().anyMatch( m ->
                m.getFormattedMessage().contains("HTTP RESPONSE DATA:\n"
                        + "HTTP STATUS: 200 OK\n"
                        + "BODY: \n"
                        + "END HTTP (0-byte body)")));
    }
}
