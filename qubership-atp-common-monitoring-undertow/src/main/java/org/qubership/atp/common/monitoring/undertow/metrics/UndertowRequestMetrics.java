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

@SuppressWarnings("checkstyle:HiddenField")
public class UndertowRequestMetrics extends UndertowMeterBinder {

    /**
     * Request Count metric name (without prefix).
     */
    private static final String METRIC_NAME_REQUESTS =          ".request.count";

    /**
     * Request Error Count metric name (without prefix).
     */
    private static final String METRIC_NAME_REQUEST_ERRORS =    ".request.errors";

    /**
     * Request Max Time metric name (without prefix).
     */
    private static final String METRIC_NAME_REQUEST_TIME_MAX =  ".request.time.max";

    /**
     * Request Min Time metric name (without prefix).
     */
    private static final String METRIC_NAME_REQUEST_TIME_MIN =  ".request.time.min";

    /**
     * Metrics Handler object.
     */
    private final MetricsHandler undertowMetricsHandler;

    /**
     * String prefix of metrics names.
     */
    private final String namePrefix;

    /**
     * Collection of tags.
     */
    private final Iterable<Tag> tags;

    /**
     * Constructor.
     *
     * @param undertowMetricsHandler MetricsHandler object.
     */
    public UndertowRequestMetrics(final MetricsHandler undertowMetricsHandler) {
        this(undertowMetricsHandler, UNDERTOW_METRIC_NAME_PREFIX);
    }

    /**
     * Constructor.
     *
     * @param undertowMetricsHandler MetricsHandler object
     * @param namePrefix String prefix of metrics names.
     */
    public UndertowRequestMetrics(final MetricsHandler undertowMetricsHandler, final String namePrefix) {
        this(undertowMetricsHandler, namePrefix, Collections.emptyList());
    }

    /**
     * Constructor.
     *
     * @param undertowMetricsHandler MetricsHandler object
     * @param namePrefix String prefix of metrics names
     * @param tags Collection of tags.
     */
    public UndertowRequestMetrics(final MetricsHandler undertowMetricsHandler,
                                  final String namePrefix,
                                  final Iterable<Tag> tags) {
        this.undertowMetricsHandler = undertowMetricsHandler;
        this.namePrefix = namePrefix;
        this.tags = tags;
    }

    /**
     * Bind custom metrics to MeterRegistry given.
     *
     * @param registry MeterRegistry to bind metrics to.
     */
    @Override
    public void bindTo(@NonNull final MeterRegistry registry) {
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
