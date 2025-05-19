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

package org.qubership.atp.common.logging.adapter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public interface AtpHttpResponse {

    /**
     * Get response headers.
     *
     * @return HttpHeaders response headers
     * @throws IOException in case IO exceptions.
     */
    HttpHeaders getHeaders();

    /**
     * Get response status code as HttpStatus object.
     *
     * @return HttpStatus object
     * @throws IOException in case IO exceptions.
     */
    HttpStatus getStatusCode() throws IOException;

    /**
     * Get response status code value as String.
     *
     * @return String response status code value
     * @throws IOException in case IO exceptions.
     */
    int getStatusCodeValue() throws IOException;

    /**
     * Get response status code reason phrase String.
     *
     * @return String response status code reason phrase
     * @throws IOException in case IO exceptions.
     */
    String getStatusCodeReason() throws IOException;

    /**
     * Get response body as String.
     *
     * @return String response body
     * @throws IOException in case IO exceptions.
     */
    String getBody() throws IOException;
}
