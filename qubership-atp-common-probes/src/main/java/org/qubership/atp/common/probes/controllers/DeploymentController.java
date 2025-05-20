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

package org.qubership.atp.common.probes.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DeploymentController {

    /**
     * ApplicationAvailability object.
     */
    private final ApplicationAvailability applicationAvailability;

    /**
     * Constructor.
     *
     * @param applicationAvailability ApplicationAvailability object.
     */
    @Autowired
    public DeploymentController(final ApplicationAvailability applicationAvailability) {
        this.applicationAvailability = applicationAvailability;
    }

    /**
     * Return response for livenessProbe.
     *
     * @return ResponseEntity with OK (in case Correct liveness state) or INTERNAL_SERVER_ERROR HttpStatus.
     */
    @GetMapping("/rest/deployment/liveness")
    public ResponseEntity<Void> liveness() {
        return new ResponseEntity<>(
                LivenessState.CORRECT.equals(applicationAvailability.getLivenessState())
                        ? HttpStatus.OK
                        : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Return response for readinessProbe.
     *
     * @return ResponseEntity with status depending on applicationAvailability.getReadinessState().
     */
    @GetMapping("/rest/deployment/readiness")
    public ResponseEntity<Void> readiness() {
        return new ResponseEntity<>(
                ReadinessState.ACCEPTING_TRAFFIC.equals(applicationAvailability.getReadinessState())
                        ? HttpStatus.OK
                        : HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
