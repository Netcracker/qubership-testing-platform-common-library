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

import java.util.concurrent.TimeUnit;

import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.lang.NonNull;
import io.undertow.server.session.SessionManagerStatistics;

public class UndertowSessionMetrics extends UndertowMetrics {

    /**
     * Sessions Max Active Count metric name (without prefix).
     */
    private static final String METRIC_NAME_SESSIONS_ACTIVE_MAX = ".sessions.active.max";

    /**
     * Sessions Current Active Count metric name (without prefix).
     */
    private static final String METRIC_NAME_SESSIONS_ACTIVE_CURRENT = ".sessions.active.current";

    /**
     * Sessions Created Count metric name (without prefix).
     */
    private static final String METRIC_NAME_SESSIONS_CREATED = ".sessions.created";

    /**
     * Sessions Expired Count metric name (without prefix).
     */
    private static final String METRIC_NAME_SESSIONS_EXPIRED = ".sessions.expired";

    /**
     * Sessions Rejected Count metric name (without prefix).
     */
    private static final String METRIC_NAME_SESSIONS_REJECTED = ".sessions.rejected";

    /**
     * Sessions Max Alive Count metric name (without prefix).
     */
    private static final String METRIC_NAME_SESSIONS_ALIVE_MAX = ".sessions.alive.max";

    /**
     * Constructor.
     *
     * @param undertowWebServer UndertowWebServer object.
     */
    public UndertowSessionMetrics(final UndertowWebServer undertowWebServer) {
        super(undertowWebServer);
    }

    /**
     * Constructor.
     *
     * @param undertowWebServer UndertowWebServer object
     * @param namePrefix String prefix to be added to metric name.
     */
    public UndertowSessionMetrics(final UndertowWebServer undertowWebServer, final String namePrefix) {
        super(undertowWebServer, namePrefix);
    }

    /**
     * Constructor.
     *
     * @param undertowWebServer UndertowWebServer object
     * @param namePrefix String prefix to be added to metric name
     * @param tags Collection of Tags.
     */
    public UndertowSessionMetrics(final UndertowWebServer undertowWebServer,
                                  final String namePrefix,
                                  final Iterable<Tag> tags) {
        super(undertowWebServer, namePrefix, tags);
    }

    /**
     * Bind MeterRegistry registry to undertowWebServer.
     *
     * @param registry MeterRegistry object
     * @param undertowWebServer UndertowWebServer object
     * @param namePrefix String prefix to be added to metric name
     * @param tags Collection of Tags.
     */
    @Override
    public void bindTo(@NonNull final MeterRegistry registry,
                       final UndertowWebServer undertowWebServer,
                       final String namePrefix,
                       final Iterable<Tag> tags) {
        if (undertowWebServer instanceof UndertowServletWebServer) {
            SessionManagerStatistics statistics = ((UndertowServletWebServer) undertowWebServer).getDeploymentManager()
                    .getDeployment()
                    .getSessionManager()
                    .getStatistics();
            registerSessionStatistics(registry, statistics, namePrefix, tags);
        }
    }

    /**
     * Register session metrics.
     *
     * @param registry Meter Registry
     * @param statistics SessionManagerStatistics object
     * @param namePrefix String prefix to be added to metric name
     * @param tags Tags.
     */
    private void registerSessionStatistics(final MeterRegistry registry,
                                           final SessionManagerStatistics statistics,
                                           final String namePrefix,
                                           final Iterable<Tag> tags) {
        Gauge.builder(namePrefix + METRIC_NAME_SESSIONS_ACTIVE_MAX,
                        statistics,
                        SessionManagerStatistics::getMaxActiveSessions)
                .tags(tags)
                .baseUnit(BaseUnits.SESSIONS)
                .register(registry);

        Gauge.builder(namePrefix + METRIC_NAME_SESSIONS_ACTIVE_CURRENT,
                        statistics,
                        SessionManagerStatistics::getActiveSessionCount)
                .tags(tags)
                .baseUnit(BaseUnits.SESSIONS)
                .register(registry);

        FunctionCounter.builder(namePrefix + METRIC_NAME_SESSIONS_CREATED,
                        statistics,
                        SessionManagerStatistics::getCreatedSessionCount)
                .tags(tags)
                .baseUnit(BaseUnits.SESSIONS)
                .register(registry);

        FunctionCounter.builder(namePrefix + METRIC_NAME_SESSIONS_EXPIRED,
                        statistics,
                        SessionManagerStatistics::getExpiredSessionCount)
                .tags(tags)
                .baseUnit(BaseUnits.SESSIONS)
                .register(registry);

        FunctionCounter.builder(namePrefix + METRIC_NAME_SESSIONS_REJECTED,
                        statistics,
                        SessionManagerStatistics::getRejectedSessions)
                .tags(tags)
                .baseUnit(BaseUnits.SESSIONS)
                .register(registry);

        TimeGauge.builder(namePrefix + METRIC_NAME_SESSIONS_ALIVE_MAX,
                        statistics,
                        TimeUnit.SECONDS,
                        SessionManagerStatistics::getHighestSessionCount)
                .tags(tags)
                .register(registry);
    }

}
