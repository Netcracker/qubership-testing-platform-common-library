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

import java.util.Arrays;
import java.util.Map;

import org.springframework.http.HttpHeaders;

import org.qubership.atp.common.logging.adapter.AtpHttpRequest;
import org.qubership.atp.common.logging.filter.CommonHttpRequestWrapper;
import org.qubership.atp.common.logging.utils.Util;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContentCachingHttpRequest implements AtpHttpRequest {

    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";

    private final CommonHttpRequestWrapper request;

    @Override
    public HttpHeaders getHeaders() {
        return Util.getHeaders(request);
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getUri() {
        StringBuilder uri = getParameters();
        uri.insert(0, request.getRequestURI());
        return uri.toString();
    }

    /**
     * Get request body.
     *
     * @return byte[] body.
     */
    @Override
    public byte[] getBody() {
        return request.getContent();
    }

    private StringBuilder getParameters() {
        StringBuilder uri = new StringBuilder();
        Map<String, String[]> parameters = request.getParameterMap();
        if (!parameters.isEmpty()) {
            parameters.forEach((key, val) ->
                Arrays.stream(val).forEach(value -> uri.append(AMPERSAND).append(key).append(EQUALS).append(value)));
            uri.replace(0, 1, QUESTION_MARK);
        }
        return uri;
    }
}
