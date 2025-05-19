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

package org.qubership.atp.common.logging.config;

import org.qubership.atp.common.logging.logger.AtpSlf4jLogger;
import org.springframework.cloud.openfeign.FeignLoggerFactory;

import feign.Logger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AtpFeignLoggerFactory implements FeignLoggerFactory {

    /**
     * LoggingProperties object.
     */
    private final LoggingProperties loggingProperties;

    /**
     * Create logger of specified type with loggingProperties configured.
     *
     * @param type Class of Logger to be created
     * @return new AtpSlf4jLogger for the type and loggingProperties configured.
     */
    @Override
    public Logger create(final Class<?> type) {
        return new AtpSlf4jLogger(type, loggingProperties);
    }
}
