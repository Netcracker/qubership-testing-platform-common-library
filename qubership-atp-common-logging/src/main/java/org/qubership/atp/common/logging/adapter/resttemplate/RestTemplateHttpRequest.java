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

package org.qubership.atp.common.logging.adapter.resttemplate;

import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestTemplateHttpRequest implements AtpHttpRequest {

    /**
     * HttpRequest object.
     */
    private final HttpRequest request;

    /**
     * Request body.
     */
    private final byte[] body;

    /**
     * Get Request Http Headers.
     *
     * @return HttpHeaders object.
     */
    @Override
    public HttpHeaders getHeaders() {
        return request.getHeaders();
    }

    /**
     * Get Request Method.
     *
     * @return String request method.
     */
    @Override
    public String getMethod() {
        return request.getMethodValue();
    }

    /**
     * Get Request URI.
     *
     * @return String request URI.
     */
    @Override
    public String getUri() {
        return request.getURI().toString();
    }

    /**
     * Get request body.
     *
     * @return byte[] body.
     */
    @Override
    public byte[] getBody() {
        return body;
    }
}
