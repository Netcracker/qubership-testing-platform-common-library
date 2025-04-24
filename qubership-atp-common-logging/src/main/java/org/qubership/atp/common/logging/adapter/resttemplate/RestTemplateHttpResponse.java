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

import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.util.StreamUtils.copyToString;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import org.qubership.atp.common.logging.adapter.AtpHttpResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestTemplateHttpResponse implements AtpHttpResponse {

    private final ClientHttpResponse response;

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    public int getStatusCodeValue() throws IOException {
        return response.getRawStatusCode();
    }

    @Override
    public String getStatusCodeReason() throws IOException {
        return response.getStatusText();
    }

    @Override
    public String getBody() throws IOException {
        return copyToString(response.getBody(), defaultCharset());
    }
}
