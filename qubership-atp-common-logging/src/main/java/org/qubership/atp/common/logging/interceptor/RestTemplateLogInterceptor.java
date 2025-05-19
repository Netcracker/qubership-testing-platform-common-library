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

package org.qubership.atp.common.logging.interceptor;

import static org.qubership.atp.common.logging.utils.Util.logMessage;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import org.qubership.atp.common.logging.adapter.AtpHttpResponse;
import org.qubership.atp.common.logging.adapter.resttemplate.RestTemplateHttpRequest;
import org.qubership.atp.common.logging.adapter.resttemplate.RestTemplateHttpResponse;
import org.qubership.atp.common.logging.config.LoggingProperties;
import org.qubership.atp.common.logging.utils.Util;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RestTemplateLogInterceptor implements ClientHttpRequestInterceptor {

    /**
     * LoggingProperties object.
     */
    private final LoggingProperties loggingProperties;

    /**
     * Perform intercept of request.
     *
     * @param request HttpRequest object
     * @param bytes byte[] request body
     * @param clientHttpRequestExecution Tasks executor
     * @return ClientHttpResponse object
     * @throws IOException in case IO errors occurred.
     */
    @Nonnull
    @Override
    public ClientHttpResponse intercept(@Nonnull final HttpRequest request,
                                        @Nonnull final byte[] bytes,
                                        @Nonnull final ClientHttpRequestExecution clientHttpRequestExecution)
        throws IOException {
        logRequest(request, bytes);
        ClientHttpResponse response = clientHttpRequestExecution.execute(request, bytes);
        logResponse(response);
        return response;
    }

    private void logRequest(final HttpRequest httpRequest, final byte[] body) {
        AtpHttpRequest request = new RestTemplateHttpRequest(httpRequest, body);
        Boolean isLoggedHeaders = loggingProperties.logRestTemplateHeaders();
        List<Pattern> ignoreHeadersPattern = loggingProperties.getIgnoreRestTemplateHeaderPatterns();
        List<String> logs = Util.getLogRequestData(request, isLoggedHeaders, ignoreHeadersPattern);
        logMessage(log, String.join(StringUtils.LF, logs));
    }

    private void logResponse(final ClientHttpResponse httpResponse) throws IOException {
        AtpHttpResponse response = new RestTemplateHttpResponse(httpResponse);
        Boolean isLoggedHeaders = loggingProperties.logRestTemplateHeaders();
        List<Pattern> ignoreHeadersPattern = loggingProperties.getIgnoreRestTemplateHeaderPatterns();
        String body = response.getBody();
        List<String> logs = Util.getLogResponseData(response, body, isLoggedHeaders, ignoreHeadersPattern);
        logMessage(log, String.join(StringUtils.LF, logs));
    }
}
