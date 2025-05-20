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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.web.embedded.undertow.UndertowWebServer;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.lang.NonNull;
import io.undertow.Undertow;
import io.undertow.server.ConnectorStatistics;

public class UndertowConnectorMetrics extends UndertowMetrics {

    /**
     * Connectors Requests Count metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_REQUESTS_COUNT 			= ".connectors.requests.count";

    /**
     * Connectors Requests Error Count metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_REQUESTS_ERROR_COUNT 	= ".connectors.requests.error.count";

    /**
     * Connectors Active Requests Count metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_REQUESTS_ACTIVE 			= ".connectors.requests.active";

    /**
     * Connectors Maximum Active Requests Limit metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_REQUESTS_ACTIVE_MAX 		= ".connectors.requests.active.max";

    /**
     * Connectors Bytes sent metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_BYTES_SENT 				= ".connectors.bytes.sent";

    /**
     * Connectors Bytes received metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_BYTES_RECEIVED 			= ".connectors.bytes.received";

    /**
     * Connectors Processing Time metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_PROCESSING_TIME 			= ".connectors.processing.time";

    /**
     * Connectors Maximum Processing Time Limit metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_PROCESSING_TIME_MAX 		= ".connectors.processing.time.max";

    /**
     * Connectors Active Connections Count metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_CONNECTIONS_ACTIVE 		= ".connectors.connections.active";

    /**
     * Connectors Maximum Active Connections Limit metric name (without prefix).
     */
    private static final String METRIC_NAME_CONNECTORS_CONNECTIONS_ACTIVE_MAX 	= ".connectors.connections.active.max";

    /**
     * Constant for Protocol Tag Name.
     */
    private static final String METRIC_TAG_PROTOCOL = "protocol";

    /**
     * Constructor.
     *
     * @param undertowWebServer UndertowWebServer object.
     */
    public UndertowConnectorMetrics(final UndertowWebServer undertowWebServer) {
        super(undertowWebServer);
    }

    /**
     * Constructor.
     *
     * @param undertowWebServer UndertowWebServer object
     * @param namePrefix String prefix to filter names.
     */
    public UndertowConnectorMetrics(final UndertowWebServer undertowWebServer, final String namePrefix) {
        super(undertowWebServer, namePrefix);
    }

    /**
     * Constructor.
     *
     * @param undertowWebServer UndertowWebServer object
     * @param namePrefix String prefix to filter names
     * @param tags Tags collection.
     */
    public UndertowConnectorMetrics(final UndertowWebServer undertowWebServer,
                                    final String namePrefix,
                                    final Iterable<Tag> tags) {
        super(undertowWebServer, namePrefix, tags);
    }

    /**
     * Bind metrics to the given registry.
     *
     * @param registry MeterRegistry object
     * @param undertow Undertow server
     * @param namePrefix String prefix to filter names
     * @param tags Tags collection.
     */
    @Override
    public void bindTo(@NonNull final MeterRegistry registry,
                       final Undertow undertow,
                       final String namePrefix,
                       final Iterable<Tag> tags) {
        List<Undertow.ListenerInfo> listenerInfoList = undertow.getListenerInfo();
        listenerInfoList.forEach(listenerInfo -> registerConnectorStatistics(registry, listenerInfo, namePrefix, tags));
    }

    private void registerConnectorStatistics(final MeterRegistry registry,
                                             final Undertow.ListenerInfo listenerInfo,
                                             final String namePrefix,
                                             final Iterable<Tag> tags) {
        String protocol = listenerInfo.getProtcol();
        ConnectorStatistics statistics = listenerInfo.getConnectorStatistics();
        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_REQUESTS_COUNT,
                        statistics,
                        ConnectorStatistics::getRequestCount)
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .register(registry);
        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_REQUESTS_ERROR_COUNT,
                        statistics,
                        ConnectorStatistics::getErrorCount)
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .register(registry);
        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_REQUESTS_ACTIVE,
                        statistics,
                        ConnectorStatistics::getActiveRequests)
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .baseUnit(BaseUnits.CONNECTIONS)
                .register(registry);
        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_REQUESTS_ACTIVE_MAX,
                        statistics,
                        ConnectorStatistics::getMaxActiveRequests)
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .baseUnit(BaseUnits.CONNECTIONS)
                .register(registry);

        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_BYTES_SENT,
                        statistics,
                        ConnectorStatistics::getBytesSent)
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .baseUnit(BaseUnits.BYTES)
                .register(registry);
        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_BYTES_RECEIVED,
                        statistics,
                        ConnectorStatistics::getBytesReceived)
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .baseUnit(BaseUnits.BYTES)
                .register(registry);

        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_PROCESSING_TIME,
                        statistics,
                        (s) -> TimeUnit.NANOSECONDS.toMillis(s.getProcessingTime()))
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .baseUnit(BaseUnits.MILLISECONDS)
                .register(registry);
        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_PROCESSING_TIME_MAX,
                        statistics,
                        (s) -> TimeUnit.NANOSECONDS.toMillis(s.getMaxProcessingTime()))
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .baseUnit(BaseUnits.MILLISECONDS)
                .register(registry);

        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_CONNECTIONS_ACTIVE,
                        statistics,
                        ConnectorStatistics::getActiveConnections)
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .baseUnit(BaseUnits.CONNECTIONS)
                .register(registry);
        Gauge.builder(namePrefix + METRIC_NAME_CONNECTORS_CONNECTIONS_ACTIVE_MAX,
                        statistics,
                        ConnectorStatistics::getMaxActiveConnections)
                .tags(tags)
                .tag(METRIC_TAG_PROTOCOL, protocol)
                .baseUnit(BaseUnits.CONNECTIONS)
                .register(registry);
    }

}
