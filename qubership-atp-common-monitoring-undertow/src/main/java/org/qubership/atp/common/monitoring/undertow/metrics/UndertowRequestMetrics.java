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

package org.qubership.atp.common.monitoring.undertow.metrics;

import java.util.Collections;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.lang.NonNull;
import io.undertow.server.handlers.MetricsHandler;

public class UndertowRequestMetrics extends UndertowMeterBinder {

    private static final String METRIC_NAME_REQUESTS =          ".request.count";
    private static final String METRIC_NAME_REQUEST_ERRORS =    ".request.errors";
    private static final String METRIC_NAME_REQUEST_TIME_MAX =  ".request.time.max";
    private static final String METRIC_NAME_REQUEST_TIME_MIN =  ".request.time.min";

    private final MetricsHandler undertowMetricsHandler;
    private final String namePrefix;
    private final Iterable<Tag> tags;

    public UndertowRequestMetrics(MetricsHandler undertowMetricsHandler) {
        this(undertowMetricsHandler, UNDERTOW_METRIC_NAME_PREFIX);
    }

    public UndertowRequestMetrics(MetricsHandler undertowMetricsHandler, String namePrefix) {
        this(undertowMetricsHandler, namePrefix, Collections.emptyList());
    }

    public UndertowRequestMetrics(MetricsHandler undertowMetricsHandler,
                                  String namePrefix,
                                  Iterable<Tag> tags) {
        this.undertowMetricsHandler = undertowMetricsHandler;
        this.namePrefix = namePrefix;
        this.tags = tags;
    }

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        bindTimer(registry, namePrefix + METRIC_NAME_REQUESTS,
                "Number of total requests",
                undertowMetricsHandler,
                m -> m.getMetrics().getTotalRequests(),
                m2 -> m2.getMetrics().getMinRequestTime(),
                tags);
        bindTimeGauge(registry, namePrefix + METRIC_NAME_REQUEST_TIME_MAX,
                "The longest request duration in time",
                undertowMetricsHandler,
                m -> m.getMetrics().getMaxRequestTime(),
                tags);
        bindTimeGauge(registry, namePrefix + METRIC_NAME_REQUEST_TIME_MIN,
                "The shortest request duration in time",
                undertowMetricsHandler,
                m -> m.getMetrics().getMinRequestTime(),
                tags);
        bindCounter(registry, namePrefix + METRIC_NAME_REQUEST_ERRORS,
                "Total number of error requests ",
                undertowMetricsHandler,
                m -> m.getMetrics().getTotalErrors(),
                tags);
    }
}
