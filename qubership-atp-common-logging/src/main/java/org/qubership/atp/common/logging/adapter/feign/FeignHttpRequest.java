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

import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import feign.Request;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeignHttpRequest implements AtpHttpRequest {

    private final Request request;

    @Override
    public HttpHeaders getHeaders() {
        return getHttpHeaders(request.headers());
    }

    @Override
    public String getMethod() {
        return request.httpMethod().name();
    }

    @Override
    public String getUri() {
        return request.url();
    }

    /**
     * Get request body.
     *
     * @return byte[] body.
     */
    @Override
    public byte[] getBody() {
        /*
            In version 10.7.4 this method becomes not deprecated.
            Use this method to support the same version of common logging
            for services with different Spring versions (2.2.2 in Orch for example).
         */
        return request.body();
    }
}
