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

import org.qubership.atp.common.logging.adapter.AtpHttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestTemplateHttpResponse implements AtpHttpResponse {

    /**
     * Http Response object.
     */
    private final ClientHttpResponse response;

    /**
     * Get Response Http Headers.
     *
     * @return HttpHeaders object.
     */
    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    /**
     * Get Response Http Status object.
     *
     * @return HttpStatus object.
     * @throws IOException in case IO exceptions.
     */
    @Override
    public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.resolve(response.getStatusCode().value());
    }

    /**
     * Get Response Http Status Code value.
     *
     * @return int Http Status Code value
     * @throws IOException in case IO exceptions.
     */
    @Override
    public int getStatusCodeValue() throws IOException {
        return response.getStatusCode().value();
    }

    /**
     * Get Response Http Status Code reason phrase.
     *
     * @return String Http Status Code reason phrase
     * @throws IOException in case IO exceptions.
     */
    @Override
    public String getStatusCodeReason() throws IOException {
        return response.getStatusText();
    }

    /**
     * Get response body as String.
     *
     * @return String response body
     * @throws IOException in case IO exceptions.
     */
    @Override
    public String getBody() throws IOException {
        return copyToString(response.getBody(), defaultCharset());
    }
}
