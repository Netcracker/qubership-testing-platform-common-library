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

package org.qubership.atp.common.logging.adapter.feign;

import static org.qubership.atp.common.logging.utils.Util.getHttpHeaders;

import org.qubership.atp.common.logging.adapter.AtpHttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import feign.Response;

public class FeignHttpResponse implements AtpHttpResponse {

    /**
     * Response object.
     */
    private final Response response;

    /**
     * Response status.
     */
    private final HttpStatus status;

    /**
     * Response body.
     */
    private final String body;

    /**
     * FeignHttpResponse constructor.
     *
     * @param response Feign response.
     * @param body     Feign response body.
     */
    public FeignHttpResponse(final Response response, final String body) {
        this.response = response;
        this.status = HttpStatus.valueOf(response.status());
        this.body = body;
    }

    /**
     * Get response headers.
     *
     * @return HttpHeaders response headers.
     */
    @Override
    public HttpHeaders getHeaders() {
        return getHttpHeaders(response.headers());
    }

    /**
     * Get response status code as HttpStatus object.
     *
     * @return HttpStatus object.
     */
    @Override
    public HttpStatus getStatusCode() {
        return status;
    }

    /**
     * Get response status code value as String.
     *
     * @return String response status code value.
     */
    @Override
    public int getStatusCodeValue() {
        return status.value();
    }

    /**
     * Get response status code reason phrase String.
     *
     * @return String response status code reason phrase.
     */
    @Override
    public String getStatusCodeReason() {
        return status.getReasonPhrase();
    }

    /**
     * Get response body.
     *
     * @return String response body.
     */
    @Override
    public String getBody() {
        return body;
    }
}
