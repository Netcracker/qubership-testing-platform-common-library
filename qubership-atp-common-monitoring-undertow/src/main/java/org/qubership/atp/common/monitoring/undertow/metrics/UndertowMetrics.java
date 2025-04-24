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

import org.springframework.boot.web.embedded.undertow.UndertowWebServer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.lang.NonNull;
import io.undertow.Undertow;

public abstract class UndertowMetrics extends UndertowMeterBinder {


    private final UndertowWebServer undertowWebServer;
    private final String namePrefix;
    private final Iterable<Tag> tags;

    public UndertowMetrics(UndertowWebServer undertowWebServer) {
        this(undertowWebServer, UNDERTOW_METRIC_NAME_PREFIX);
    }

    public UndertowMetrics(UndertowWebServer undertowWebServer, String namePrefix) {
        this(undertowWebServer, namePrefix, Collections.emptyList());
    }

    public UndertowMetrics(UndertowWebServer undertowWebServer, String namePrefix, Iterable<Tag> tags) {
        this.undertowWebServer = undertowWebServer;
        this.namePrefix = namePrefix;
        this.tags = tags;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        bindTo(registry, undertowWebServer, namePrefix, tags);
        bindTo(registry, getUndertow(undertowWebServer), namePrefix, tags);

    }

    void bindTo(@NonNull MeterRegistry registry,
                UndertowWebServer undertowWebServer,
                String namePrefix,
                Iterable<Tag> tags) {}

    void bindTo(@NonNull MeterRegistry registry,
                Undertow undertow,
                String namePrefix,
                Iterable<Tag> tags) {}



}
