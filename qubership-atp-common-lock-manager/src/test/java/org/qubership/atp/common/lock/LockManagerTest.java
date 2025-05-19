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

import static java.lang.Thread.sleep;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.common.lock.provider.InMemoryLockProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MagicNumber")
public class LockManagerTest {

    /**
     * LockManager for tests.
     */
    private LockManager lockManager;

    /**
     * Init lockManager before tests.
     */
    @Before
    public void setUp() {
        lockManager = new LockManager(60, 20, 3, new InMemoryLockProvider());
    }

    /**
     * Test when executing with lock and wait lock enough time then lock is obtained.
     *
     * @throws ExecutionException in case task execution exceptions
     * @throws InterruptedException in case execution is interrupted (or before trying it).
     */
    @Test
    public void executeWithLockWaitLockThenLockObtained() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Boolean> task1 = executor.submit(
                () -> lockManager.executeWithLock("a", 15, () -> {
                    log.error("Wait lock - Lock is obtained: the 1st call");
                    sleep(15000);
                    return true;
                }, () -> false));

        sleep(1000);

        Future<Boolean> task2 = executor.submit(
                () -> lockManager.executeWithLock("a", 15, () -> {
                    log.error("Wait lock - Lock is obtained: the 2nd call");
                    return true;
                }, () -> false));

        Boolean result1 = task1.get();
        Boolean result2 = task2.get();
        Assert.assertTrue(result1);
        Assert.assertTrue(result2);
    }

    /**
     * Test when executing with lock and wait lock not enough time then lock isn't obtained.
     *
     * @throws ExecutionException in case task execution exceptions
     * @throws InterruptedException in case execution is interrupted (or before trying it).
     */
    @Test
    public void executeWithLockWaitLockThenLockNotObtained() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Boolean> task1 = executor.submit(
                () -> lockManager.executeWithLock("a", 60, () -> {
                    log.error("Wait lock - Lock isn't obtained: the 1st call");
                    sleep(30000);
                    return true;
                }, () -> false));

        sleep(1000);

        Future<Boolean> task2 = executor.submit(
                () -> lockManager.executeWithLock("a", 10, () -> {
                    log.error("Wait lock - Lock isn't obtained: the 2nd call");
                    return true;
                }, () -> false));

        Boolean result1 = task1.get();
        Boolean result2 = task2.get();
        Assert.assertTrue(result1);
        Assert.assertFalse(result2);
    }

    /**
     * Test when executing with lock and wait lock unsuccessfully then exception is thrown.
     */
    @Test
    public void executeWithLockWaitLockThenExceptionThrown() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Boolean> task1 = executor.submit(
                () -> lockManager.executeWithLock("a", () -> {
                    throw new RuntimeException("Exception from thread-1");
                }, () -> false));

        Throwable result = null;
        try {
            task1.get();
        } catch (Exception e) {
            result = e.getCause();
        }

        Assert.assertTrue(result instanceof RuntimeException);
        Assert.assertEquals("Exception from thread-1", result.getMessage());
    }

    /**
     * Test when executing with lock but don't wait then lock isn't obtained.
     *
     * @throws ExecutionException in case task execution exceptions
     * @throws InterruptedException in case execution is interrupted (or before trying it).
     */
    @Test
    public void executeWithLockNoWaitThenLockNotObtained() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Boolean> task1 = executor.submit(
                () -> lockManager.executeWithLockNoWait("a", () -> {
                    log.error("NoWait - Lock isn't obtained: the 1st call");
                    sleep(15000);
                    return true;
                }, () -> false));

        sleep(1000);

        Future<Boolean> task2 = executor.submit(
                () -> lockManager.executeWithLockNoWait("a", 10, () -> {
                    log.error("NoWait - Lock isn't obtained: the 2nd call");
                    return true;
                }, () -> false));

        Boolean result1 = task1.get();
        Boolean result2 = task2.get();
        Assert.assertTrue(result1);
        Assert.assertFalse(result2);
    }
}
