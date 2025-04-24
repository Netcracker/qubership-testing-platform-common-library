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

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.qubership.atp.common.logging.filter.LoggingFilter;
import org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor;

@Configuration
public class LoggingConfiguration {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Bean
    public LoggingProperties headerProperties() {
        return new LoggingProperties();
    }

    @Bean
    public LoggingFilter getLogFilter(LoggingProperties loggingProperties) {
        return new LoggingFilter(loggingProperties);
    }

    @Bean
    public FeignLoggerFactory getFeignLoggerFactory(LoggingProperties loggingProperties) {
        return new AtpFeignLoggerFactory(loggingProperties);
    }

    @Bean
    public RestTemplateLogInterceptor restTemplateLogInterceptor(LoggingProperties loggingProperties) {
        return new RestTemplateLogInterceptor(loggingProperties);
    }
}
