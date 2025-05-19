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

package org.qubership.atp.common.lock.config;

import org.qubership.atp.common.lock.LockManager;
import org.qubership.atp.common.lock.provider.InMemoryLockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.javacrumbs.shedlock.core.LockProvider;

@Configuration
public class AtpLockConfig {

    /**
     * Default lock duration (seconds).
     */
    @Value("${atp.lock.default.duration.sec:60}")
    private Integer defaultLockDurationSec;

    /**
     * Lock retry timeout (seconds).
     */
    @Value("${atp.lock.retry.timeout.sec:10800}")
    private Integer retryTimeoutSec;

    /**
     *  Lock retry interval (seconds).
     */
    @Value("${atp.lock.retry.pace.sec:3}")
    private Integer retryPaceSec;

    /**
     * Create lockProvider bean.
     *
     * @return new InMemoryLockProvider object.
     */
    @Bean
    @ConditionalOnMissingBean
    public LockProvider lockProvider() {
        return new InMemoryLockProvider();
    }

    /**
     * Create lockManager bean given LockProvider.
     *
     * @param lockProvider LockProvider object
     * @return new LockManager object created and configured.
     */
    @Bean
    public LockManager lockManager(final LockProvider lockProvider) {
        return new LockManager(defaultLockDurationSec, retryTimeoutSec, retryPaceSec, lockProvider);
    }
}
