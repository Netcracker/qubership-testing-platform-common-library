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

import org.springframework.http.HttpHeaders;

public interface AtpHttpRequest {

    /**
     * Get request Headers.
     *
     * @return HttpHeaders headers.
     */
    HttpHeaders getHeaders();

    /**
     * Get request Method.
     *
     * @return String method.
     */
    String getMethod();

    /**
     * Get request Uri.
     *
     * @return String Uri.
     */
    String getUri();

    /**
     * Get request body.
     *
     * @return byte[] body.
     */
    byte[] getBody();
}
