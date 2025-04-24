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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestTemplateHttpRequest implements AtpHttpRequest {

    private final HttpRequest request;
    private final byte[] body;

    @Override
    public HttpHeaders getHeaders() {
        return request.getHeaders();
    }

    @Override
    public String getMethod() {
        return request.getMethodValue();
    }

    @Override
    public String getUri() {
        return request.getURI().toString();
    }

    @Override
    public byte[] getBody() {
        return body;
    }
}
