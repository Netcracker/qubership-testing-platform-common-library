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

package org.qubership.atp.common.lock;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.qubership.atp.common.lock.exceptions.AtpLockRejectException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;

@Slf4j
@SuppressWarnings("checkstyle:HiddenField")
public class LockManager {

    /**
     * Default lock duration (seconds).
     */
    private final Integer defaultLockDurationSec;

    /**
     * Retry timeout (seconds).
     */
    private final Integer retryTimeoutSec;

    /**
     * Retry interval (seconds).
     */
    private final Integer retryPaceSec;

    /**
     * Executor of tasks.
     */
    private DefaultLockingTaskExecutor defaultLockingTaskExecutor;

    /**
     * Max key size in DB (is character varying(100) actually).
     */
    private static final int MAX_KEY_SIZE = 99;

    /**
     * Instantiates a new Lock manager.
     *
     * @param defaultLockDurationSec the default lock duration sec
     * @param retryTimeoutSec        the retry time out
     * @param retryPaceSec           the retry pace sec
     * @param lockProvider           the lock provider
     */
    public LockManager(final Integer defaultLockDurationSec,
                       final Integer retryTimeoutSec,
                       final Integer retryPaceSec,
                       final LockProvider lockProvider) {
        this.defaultLockDurationSec = defaultLockDurationSec;
        this.retryTimeoutSec = retryTimeoutSec;
        this.retryPaceSec = retryPaceSec;
        this.defaultLockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider);
    }

    /**
     * Execute with lock, no wait.
     *
     * @param <T>             the type parameter
     * @param lockKey         the lock key
     * @param callable        the callable
     * @param defaultOnReject the default on skip
     * @return the result of callable
     */
    public <T> T executeWithLockNoWait(final String lockKey,
                                       final Callable<T> callable,
                                       final Supplier<T> defaultOnReject) {
        log.debug("start executeWithLockNoWait(lockKey: {})", lockKey);
        return executeWithLockNoWait(lockKey, defaultLockDurationSec, callable, defaultOnReject);
    }

    /**
     * Execute with lock no wait.
     *
     * @param lockKey  the lock key
     * @param runnable the runnable
     */
    public void executeWithLockNoWait(final String lockKey, final Runnable runnable) {
        log.debug("start executeWithLockNoWait(lockKey: {}, Runnable)", lockKey);
        executeWithLockNoWait(lockKey, defaultLockDurationSec, runnable);
    }

    /**
     * Execute with lock, no wait.
     *
     * @param <T>             the type parameter
     * @param lockKey         the lock key
     * @param lockDuration    the lock duration
     * @param callable        the callable
     * @param defaultOnReject the default on skip
     * @return the result of callable
     */
    public <T> T executeWithLockNoWait(final String lockKey, final Integer lockDuration, final Callable<T> callable,
                                       final Supplier<T> defaultOnReject) {
        log.debug("start executeWithLockNoWait(lockKey: {}, lockDuration: {})", lockKey, lockDuration);
        try {
            return executeWithLock(lockKey, lockDuration, callable);
        } catch (AtpLockRejectException e) {
            log.error("Cannot obtain lock by key {}. Lock duration {} sec.", lockKey, lockDuration);
            return defaultOnReject.get();
        }
    }

    /**
     * Execute with lock no wait.
     *
     * @param lockKey      the lock key
     * @param lockDuration the lock duration
     * @param runnable     the runnable
     */
    public void executeWithLockNoWait(final String lockKey, final Integer lockDuration, final Runnable runnable) {
        log.debug("start executeWithLockNoWait(lockKey: {}, lockDuration: {}, Runnable)", lockKey, lockDuration);
        try {
            executeWithLock(lockKey, lockDuration, runnable);
        } catch (AtpLockRejectException e) {
            log.error("Cannot obtain lock by key {}. Lock duration {} sec.", lockKey, lockDuration);
        }
    }

    /**
     * Execute with lock t.
     *
     * @param <T>             the type parameter
     * @param lockKey         the lock key
     * @param callable        the callable
     * @param defaultOnReject the default on reject
     * @return the t
     */
    public <T> T executeWithLock(final String lockKey, final Callable<T> callable, final Supplier<T> defaultOnReject) {
        log.debug("start executeWithLock(lockKey: {})", lockKey);
        return executeWithLock(lockKey, defaultLockDurationSec, callable, defaultOnReject);
    }

    /**
     * Execute with lock.
     *
     * @param lockKey  the lock key
     * @param runnable the runnable
     */
    public void executeWithLock(final String lockKey, final Runnable runnable) {
        log.debug("start executeWithLock(lockKey: {}, Runnable)", lockKey);
        executeWithLock(lockKey, defaultLockDurationSec, runnable);
    }

    /**
     * Execute with lock, and wait if busy.
     *
     * @param <T>             the type parameter
     * @param lockKey         the lock key
     * @param lockDurationSec the lock duration
     * @param callable        the callable
     * @param defaultOnReject the default on skip
     * @return the result of callable
     */
    public <T> T executeWithLock(final String lockKey, final Integer lockDurationSec, final Callable<T> callable,
                                 final Supplier<T> defaultOnReject) {
        log.debug("start executeWithLock(lockKey: {}, lockDurationSec: {})", lockKey, lockDurationSec);
        try {
            return getRetryTemplate()
                    .execute(retryContext -> executeWithLock(lockKey, lockDurationSec, callable));
        } catch (AtpLockRejectException e) {
            log.error("Cannot obtain lock by key {}. Lock duration {} sec. Returning default value.", lockKey,
                    lockDurationSec, e);
            return defaultOnReject.get();
        }
    }

    /**
     * Execute with lock.
     *
     * @param lockKey         the lock key
     * @param lockDurationSec the lock duration sec
     * @param runnable        the runnable
     */
    public void executeWithLock(final String lockKey, final Integer lockDurationSec, final Runnable runnable) {
        executeWithLock(lockKey, lockDurationSec, () -> {
            runnable.run();
            return null;
        }, () -> null);
    }

    @SneakyThrows
    private <T> T executeWithLock(final String lockKey, final Integer lockDuration, final Callable<T> callable) {
        log.debug("start executeWithLock(lockKey: {}, lockDuration: {})", lockKey, lockDuration);
        Duration lockAtMostUntil = Duration.ofSeconds(lockDuration);
        Duration lockAtLeastFor = Duration.ZERO;

        LockConfiguration lockConfiguration = new LockConfiguration(lockKey, lockAtMostUntil, lockAtLeastFor);

        LockingTaskExecutor.TaskResult<T> taskResult =
                defaultLockingTaskExecutor.executeWithLock(callable::call, lockConfiguration);

        log.debug("end executeWithLock(lockKey: {}, lockDuration: {}), taskResult.wasExecuted: {}",
                lockKey, lockDuration, taskResult.wasExecuted());
        if (!taskResult.wasExecuted()) {
            log.debug("Cannot obtain lock by key '{}'", lockKey);
            throw new AtpLockRejectException("Cannot obtain lock by key " + lockKey);
        } else {
            return taskResult.getResult();
        }
    }

    /**
     * Execute with lock. Current time mills will be added to lock key.
     *
     * @param lockKey  the lock key
     * @param runnable the runnable
     */
    public void executeWithLockWithUniqueLockKey(final String lockKey, final Runnable runnable) {
        String lockKeyWithTimeMills = lockKey + " " + System.currentTimeMillis();
        String preparedLockKey = lockKeyWithTimeMills.length() > MAX_KEY_SIZE
                ? lockKeyWithTimeMills.substring(0, MAX_KEY_SIZE) : lockKeyWithTimeMills;
        log.debug("start executeWithLock(lockKey: {}, preparedLockKey: {} Runnable)", lockKey, preparedLockKey);
        executeWithLock(preparedLockKey, defaultLockDurationSec, runnable);
    }

    private RetryTemplate getRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.setBackOffPolicy(getFixedBackOffPolicy());
        retryTemplate.setRetryPolicy(getRetryPolicy());

        return retryTemplate;
    }

    private RetryPolicy getRetryPolicy() {
        TimeoutRetryPolicy timeoutRetryPolicy = new TimeoutRetryPolicy();
        timeoutRetryPolicy.setTimeout(TimeUnit.SECONDS.toMillis(retryTimeoutSec));

        SimpleRetryPolicy exceptionTypeRetryPolicy = new SimpleRetryPolicy(Integer.MAX_VALUE,
                Collections.singletonMap(AtpLockRejectException.class, true));

        CompositeRetryPolicy compositeRetryPolicy = new CompositeRetryPolicy();
        compositeRetryPolicy.setPolicies(new RetryPolicy[]{timeoutRetryPolicy, exceptionTypeRetryPolicy});
        return compositeRetryPolicy;
    }

    private BackOffPolicy getFixedBackOffPolicy() {
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(TimeUnit.SECONDS.toMillis(retryPaceSec));
        return fixedBackOffPolicy;
    }
}
