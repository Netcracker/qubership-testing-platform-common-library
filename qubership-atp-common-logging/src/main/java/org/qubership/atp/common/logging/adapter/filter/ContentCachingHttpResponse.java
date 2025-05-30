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

package org.qubership.atp.common.logging.adapter.filter;

import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.util.StreamUtils.copyToString;

import java.io.IOException;

import org.qubership.atp.common.logging.adapter.AtpHttpResponse;
import org.qubership.atp.common.logging.utils.Util;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingResponseWrapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContentCachingHttpResponse implements AtpHttpResponse {

    /**
     * Wrapped response.
     */
    private final ContentCachingResponseWrapper response;

    /**
     * Response Http Status.
     */
    private final HttpStatus status;

    /**
     * Constructor from wrapper.
     *
     * @param response ContentCachingResponseWrapper wrapped response.
     */
    public ContentCachingHttpResponse(final ContentCachingResponseWrapper response) {
        this.response = response;
        this.status = HttpStatus.valueOf(response.getStatus());
    }

    /**
     * Get Response Http Headers.
     *
     * @return HttpHeaders object.
     */
    @Override
    public HttpHeaders getHeaders() {
        return Util.getHeaders(response);
    }

    /**
     * Get Response Http Status Code object.
     *
     * @return HttpStatus object.
     */
    @Override
    public HttpStatus getStatusCode() {
        return status;
    }

    /**
     * Get Response Http Status Code value.
     *
     * @return int http status code value.
     */
    @Override
    public int getStatusCodeValue() {
        return status.value();
    }

    /**
     * Get Response Http Status Code phrase.
     *
     * @return String http status code phrase.
     */
    @Override
    public String getStatusCodeReason() {
        return status.getReasonPhrase();
    }

    /**
     * Get response body as String.
     *
     * @return String response body
     * @throws IOException in case IO exceptions.
     */
    @Override
    public String getBody() throws IOException {
        return copyToString(response.getContentInputStream(), defaultCharset());
    }
}
