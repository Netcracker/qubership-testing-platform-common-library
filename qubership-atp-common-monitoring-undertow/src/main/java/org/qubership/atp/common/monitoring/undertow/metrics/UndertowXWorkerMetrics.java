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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.xnio.management.XnioWorkerMXBean;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.lang.NonNull;
import io.undertow.Undertow;

public class UndertowXWorkerMetrics extends UndertowMetrics {

    private static final String METRIC_NAME_X_WORK_WORKER_POOL_MAX_SIZE =       ".xwork.worker.pool.max.size";
    private static final String METRIC_NAME_X_WORK_WORKER_POOL_SIZE =           ".xwork.worker.pool.size";
    private static final String METRIC_NAME_X_WORK_WORKER_THREAD_BUSY_COUNT =   ".xwork.worker.thread.busy.count";
    private static final String METRIC_NAME_X_WORK_IO_THREAD_COUNT =            ".xwork.io.thread.count";
    private static final String METRIC_NAME_X_WORK_WORKER_QUEUE_SIZE  =         ".xwork.worker.queue.size";
    private static final String METRIC_CATEGORY = "name";

    public UndertowXWorkerMetrics(final UndertowWebServer undertowWebServer) {
        super(undertowWebServer);
    }

    public UndertowXWorkerMetrics(final UndertowWebServer undertowWebServer, final String namePrefix) {
        super(undertowWebServer, namePrefix);
    }

    public UndertowXWorkerMetrics(final UndertowWebServer undertowWebServer,
                                  final String namePrefix,
                                  final Iterable<Tag> tags) {
        super(undertowWebServer, namePrefix, tags);
    }

    @Override
    public void bindTo(@NonNull final MeterRegistry registry,
                       final Undertow undertow,
                       final String namePrefix,
                       final Iterable<Tag> tags) {
        XnioWorkerMXBean workerMXBean = undertow.getWorker().getMXBean();
        registerXWorker(registry, workerMXBean, namePrefix, tags);
    }

    private void registerXWorker(final MeterRegistry registry,
                                 final XnioWorkerMXBean workerMXBean,
                                 final String namePrefix,
                                 final Iterable<Tag> tags) {

        List<Tag> tagsList =  new ArrayList<>();
        if (Objects.nonNull(tags)) {
            tags.forEach(tagsList::add);
        }
        tagsList.add(Tag.of(METRIC_CATEGORY, workerMXBean.getName()));

        // Number of worker threads. The default is 8 times the number of I/O threads.
        // bindGauge(registry, namePrefix + METRIC_NAME_X_WORK_WORKER_POOL_CORE_SIZE,
        // "XWork core worker pool size", workerMXBean, XnioWorkerMXBean::getCoreWorkerPoolSize, tagsList);
        bindGauge(registry,
                namePrefix + METRIC_NAME_X_WORK_WORKER_POOL_MAX_SIZE,
                "XWork max worker pool size",
                workerMXBean,
                XnioWorkerMXBean::getMaxWorkerPoolSize,
                tagsList);
        bindGauge(registry,
                namePrefix + METRIC_NAME_X_WORK_WORKER_POOL_SIZE,
                "XWork worker pool size",
                workerMXBean,
                XnioWorkerMXBean::getWorkerPoolSize,
                tagsList);
        bindGauge(registry,
                namePrefix + METRIC_NAME_X_WORK_WORKER_THREAD_BUSY_COUNT,
                "XWork busy worker thread count",
                workerMXBean,
                XnioWorkerMXBean::getBusyWorkerThreadCount,
                tagsList);
        //  Number of I/O threads to create for the worker. The default is the number of available processors.
        bindGauge(registry,
                namePrefix + METRIC_NAME_X_WORK_IO_THREAD_COUNT,
                "XWork I/O thread count",
                workerMXBean,
                XnioWorkerMXBean::getIoThreadCount,
                tagsList);
        bindGauge(registry,
                namePrefix + METRIC_NAME_X_WORK_WORKER_QUEUE_SIZE,
                "XWork worker queue size",
                workerMXBean,
                XnioWorkerMXBean::getWorkerQueueSize,
                tagsList);
    }

}
