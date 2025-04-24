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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.qubership.atp.common.logging.adapter.AtpHttpResponse;
import feign.Response;

public class FeignHttpResponse implements AtpHttpResponse {

    private final Response response;
    private final HttpStatus status;
    private final String body;

    /**
     * FeignHttpResponse constructor.
     * @param response Feign response.
     * @param body     Feign response body.
     */
    public FeignHttpResponse(Response response, String body) {
        this.response = response;
        this.status = HttpStatus.valueOf(response.status());
        this.body = body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return getHttpHeaders(response.headers());
    }

    @Override
    public HttpStatus getStatusCode() {
        return status;
    }

    @Override
    public int getStatusCodeValue() {
        return status.value();
    }

    @Override
    public String getStatusCodeReason() {
        return status.getReasonPhrase();
    }

    @Override
    public String getBody() {
        return body;
    }
}
