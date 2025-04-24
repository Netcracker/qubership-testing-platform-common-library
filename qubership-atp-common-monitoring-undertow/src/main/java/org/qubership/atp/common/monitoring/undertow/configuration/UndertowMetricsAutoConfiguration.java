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

package org.qubership.atp.common.monitoring.undertow.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import org.qubership.atp.common.monitoring.undertow.metrics.UndertowConnectorMetrics;
import org.qubership.atp.common.monitoring.undertow.metrics.UndertowMetrics;
import org.qubership.atp.common.monitoring.undertow.metrics.UndertowMetricsHandlerWrapper;
import org.qubership.atp.common.monitoring.undertow.metrics.UndertowRequestMetrics;
import org.qubership.atp.common.monitoring.undertow.metrics.UndertowSessionMetrics;
import org.qubership.atp.common.monitoring.undertow.metrics.UndertowXWorkerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.MetricsHandler;

@ConditionalOnClass(Undertow.class)
public class UndertowMetricsAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    @Bean
    public UndertowBuilderCustomizer undertowBuilderCustomizerEnableStatistics() {
        return builder -> builder.setServerOption(UndertowOptions.ENABLE_STATISTICS, true);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Find UndertowWebServer
        UndertowWebServer undertowWebServer = UndertowMetrics.findUndertowWebServer(event.getApplicationContext());
        if (undertowWebServer == null) {
            return;
        }
        // Find Undertow
        Undertow undertow = UndertowMetrics.getUndertow(undertowWebServer);
        if (undertow == null) {
            return;
        }
        // Find MeterRegistry
        MeterRegistry registry = event.getApplicationContext().getBean(MeterRegistry.class);
        // Bind Undertow Metrics
        new UndertowConnectorMetrics(undertowWebServer).bindTo(registry);
        new UndertowSessionMetrics(undertowWebServer).bindTo(registry);
        new UndertowXWorkerMetrics(undertowWebServer).bindTo(registry);
        if (event.getApplicationContext() instanceof ServletWebServerApplicationContext) {
            MetricsHandler metricsHandler = UndertowMetricsHandlerWrapper.getMetricsHandler(
                    ((ServletWebServerApplicationContext)event.getApplicationContext()).getServletContext()
            );
            new UndertowRequestMetrics(metricsHandler).bindTo(registry);
        }

    }
}