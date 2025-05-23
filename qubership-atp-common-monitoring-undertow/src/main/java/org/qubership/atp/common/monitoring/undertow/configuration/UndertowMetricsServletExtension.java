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

package org.qubership.atp.common.monitoring.undertow.configuration;

import javax.servlet.ServletContext;

import org.qubership.atp.common.monitoring.undertow.metrics.UndertowMetricsHandlerWrapper;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

public class UndertowMetricsServletExtension implements ServletExtension {

    /**
     * Constructor.
     */
    public UndertowMetricsServletExtension() {
    }

    /**
     * Add UndertowMetricsHandlerWrapper to DeploymentInfo object parameter.
     *
     * @param deploymentInfo DeploymentInfo object to handle
     * @param servletContext ServletContext context to wrap into UndertowMetricsHandlerWrapper.
     */
    @Override
    public void handleDeployment(final DeploymentInfo deploymentInfo, final ServletContext servletContext) {
        deploymentInfo.addOuterHandlerChainWrapper(new UndertowMetricsHandlerWrapper(servletContext));
    }
}
