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

package org.qubership.atp.common.lock.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.support.AbstractStorageAccessor;

public class InMemoryStorageAccessorAccessor extends AbstractStorageAccessor {

    /**
     * Map of LockConfiguration by configuration name.
     */
    private final Map<String, LockConfiguration> storage = new ConcurrentHashMap<>();

    /**
     * Insert lock record by the name of lockConfiguration.
     *
     * @param lockConfiguration LockConfiguration object
     * @return always returns true.
     */
    @Override
    public boolean insertRecord(final LockConfiguration lockConfiguration) {
        storage.put(lockConfiguration.getName(), lockConfiguration);
        return true;
    }

    /**
     * Update lock record by the name of lockConfiguration.
     * Actually, 'update' is performed only if there was no lock with that name or lock was already expired.
     * For both cases, in fact, insert is performed instead of update. And method returns true.
     * Otherwise, existing lock remains unchanged and method returns false.
     *
     * @param lockConfiguration LockConfiguration object
     * @return true if there was no lock with that name or lock was already expired; otherwise false.
     */
    @Override
    public boolean updateRecord(final LockConfiguration lockConfiguration) {
        LockConfiguration currentConfig = storage.get(lockConfiguration.getName());
        if (currentConfig == null
                || currentConfig.getLockAtMostUntil().isBefore(lockConfiguration.getLockAtMostUntil())) {
            storage.put(lockConfiguration.getName(), lockConfiguration);
            return true;
        }
        return false;
    }

    /**
     * Unlock by the name of lockConfiguration.
     *
     * @param lockConfiguration LockConfiguration object
     */
    @Override
    public void unlock(final LockConfiguration lockConfiguration) {
        storage.remove(lockConfiguration.getName());
    }

    /**
     * Extend lock according to lockConfiguration properties.
     *
     * @param lockConfiguration LockConfiguration object
     * @return Always false; it means lock isn't extended.
     */
    @Override
    public boolean extend(final LockConfiguration lockConfiguration) {
        return false;
    }
}
