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

package org.qubership.atp.common.logging.logger;

import static org.qubership.atp.common.logging.utils.Util.getHttpHeaders;
import static feign.Util.decodeOrDefault;
import static feign.Util.toByteArray;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import org.qubership.atp.common.logging.adapter.feign.FeignHttpRequest;
import org.qubership.atp.common.logging.config.LoggingProperties;
import org.qubership.atp.common.logging.utils.Util;
import feign.Request;
import feign.Response;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AtpSlf4jLogger extends Slf4jLogger {

    private final Logger logger;
    private final LoggingProperties loggingProperties;

    /**
     * Constructor.
     *
     * @param loggerClass class to be logged
     * @param loggingProperties log settings
     */
    public AtpSlf4jLogger(final Class<?> loggerClass, final LoggingProperties loggingProperties) {
        this.logger = LoggerFactory.getLogger(loggerClass);
        this.loggingProperties = loggingProperties;
    }

    @Override
    protected void logRequest(final String configKey, final Level logLevel, final Request feignRequest) {
        AtpHttpRequest request = new FeignHttpRequest(feignRequest);
        Boolean isLoggedHeaders = loggingProperties.logFeignHeaders();
        List<Pattern> ignoreHeadersPattern = loggingProperties.getIgnoreFeignHeaderPatterns();
        List<String> logs = Util.getLogRequestData(request, isLoggedHeaders, ignoreHeadersPattern);
        log(configKey, String.join(StringUtils.LF, logs));
    }

    @Override
    protected Response logAndRebufferResponse(final String configKey,
                                              final Level level,
                                              final Response response,
                                              final long elapsedTime)
        throws IOException {
        return logger.isDebugEnabled() ? logResponse(configKey, level, response) : response;
    }

    private Response logResponse(final String configKey,
                                 final Level level,
                                 final Response feignResponse) throws IOException {
        int statusCode = feignResponse.status();
        HttpHeaders httpHeaders = getHttpHeaders(feignResponse.headers());
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        Boolean isLoggedHeaders = loggingProperties.logFeignHeaders();
        List<Pattern> ignoreHeadersPattern = loggingProperties.getIgnoreFeignHeaderPatterns();
        if (feignResponse.body() != null
                && !(statusCode == HttpStatus.NO_CONTENT.value() || statusCode == HttpStatus.RESET_CONTENT.value())) {
            byte[] bodyData = toByteArray(feignResponse.body().asInputStream());
            List<String> logs = Util.getLogResponseData(
                    httpHeaders,
                    httpStatus,
                    decodeOrDefault(bodyData, Charset.defaultCharset(), StringUtils.EMPTY),
                    isLoggedHeaders,
                    ignoreHeadersPattern);
            log(configKey, String.join(StringUtils.LF, logs));
            return feignResponse.toBuilder().body(bodyData).build();
        } else {
            List<String> logs = Util.getLogResponseData(
                    httpHeaders,
                    httpStatus,
                    StringUtils.EMPTY,
                    isLoggedHeaders,
                    ignoreHeadersPattern);
            log(configKey, String.join(StringUtils.LF, logs));
            return feignResponse;
        }
    }

    @Override
    protected void log(final String configKey, final String message, final Object... args) {
        if (this.logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }
}
