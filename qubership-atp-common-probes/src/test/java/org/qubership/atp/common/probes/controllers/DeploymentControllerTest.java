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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {DeploymentController.class})
class DeploymentControllerTest {


    @MockBean
    private ApplicationAvailability applicationAvailability;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void liveness_whenLivenessStateIsCorrect() throws Exception {
        when(applicationAvailability.getLivenessState()).thenReturn(LivenessState.CORRECT);

        mockMvc.perform(get("/rest/deployment/liveness"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void liveness_whenLivenessStateIsBroken() throws Exception {
        when(applicationAvailability.getLivenessState()).thenReturn(LivenessState.BROKEN);

        mockMvc.perform(get("/rest/deployment/liveness"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void readiness_whenReadinessStateIsAcceptingTraffic() throws Exception {
        when(applicationAvailability.getReadinessState()).thenReturn(ReadinessState.ACCEPTING_TRAFFIC);

        mockMvc.perform(get("/rest/deployment/readiness"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void readiness_whenReadinessStateIsRefusingTraffic() throws Exception {
        when(applicationAvailability.getReadinessState()).thenReturn(ReadinessState.REFUSING_TRAFFIC);

        mockMvc.perform(get("/rest/deployment/readiness"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
