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

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ReflectionUtils;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.undertow.Undertow;

public abstract class UndertowMeterBinder implements MeterBinder {

    private static final Field UNDERTOW_FIELD;
    /**
     * Prefix used for all Undertow metric names.
     */
    public static final String UNDERTOW_METRIC_NAME_PREFIX = "undertow";

    protected  <T> void bindTimer(MeterRegistry registry, String name, String desc, T metricsHandler, ToLongFunction<T> countFunc, ToDoubleFunction<T> consumer, Iterable<Tag> tags) {
        FunctionTimer.builder(name, metricsHandler, countFunc, consumer, TimeUnit.MILLISECONDS)
                .description(desc)
                .tags(tags)
                .register(registry);
    }

    protected <T> void bindGauge(MeterRegistry registry, String name, String desc, T metricResult,
                                 ToDoubleFunction<T> consumer, Iterable<Tag> tags) {
        Gauge.builder(name, metricResult, consumer)
                .description(desc)
                .tags(tags)
                .register(registry);
    }

    protected <T> void bindTimeGauge(MeterRegistry registry, String name, String desc, T metricResult,
                                     ToDoubleFunction<T> consumer, Iterable<Tag> tags) {
        TimeGauge.builder(name, metricResult, TimeUnit.MILLISECONDS, consumer)
                .description(desc)
                .tags(tags)
                .register(registry);
    }

    protected <T> void bindCounter(MeterRegistry registry, String name, String desc, T metricsHandler, ToDoubleFunction<T> consumer, Iterable<Tag> tags) {
        FunctionCounter.builder(name, metricsHandler, consumer)
                .description(desc)
                .tags(tags)
                .register(registry);
    }

    static {
        UNDERTOW_FIELD = ReflectionUtils.findField(UndertowWebServer.class, "undertow");
        Objects.requireNonNull(UNDERTOW_FIELD, "UndertowWebServer class field undertow not exist.");
        ReflectionUtils.makeAccessible(UNDERTOW_FIELD);
    }

    public static UndertowWebServer findUndertowWebServer(ConfigurableApplicationContext applicationContext) {
        WebServer webServer;
        if (applicationContext instanceof ReactiveWebServerApplicationContext) {
            ReactiveWebServerApplicationContext context = (ReactiveWebServerApplicationContext) applicationContext;
            webServer = context.getWebServer();
        } else if (applicationContext instanceof ServletWebServerApplicationContext) {
            ServletWebServerApplicationContext context = (ServletWebServerApplicationContext) applicationContext;
            webServer = context.getWebServer();
        } else {
            return null;
        }
        if (webServer instanceof UndertowWebServer) {
            UndertowWebServer server = (UndertowWebServer) webServer;
            return server;
        }
        return null;
    }

    public static Undertow getUndertow(UndertowWebServer undertowWebServer) {
        return (Undertow) ReflectionUtils.getField(UNDERTOW_FIELD, undertowWebServer);
    }

}