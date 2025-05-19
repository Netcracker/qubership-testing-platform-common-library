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

package org.qubership.atp.common.logging.config;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class LoggingProperties {

    /**
     * Log Headers of Requests/Responses processed by Controllers (true) or not.
     */
    @Getter
    @Value("#{new Boolean('${atp.logging.controller.headers:false}')}")
    private Boolean logControllerHeaders;

    /**
     * Log Headers of Requests/Responses processed by Feign Clients (true) or not.
     */
    @Getter
    @Value("#{new Boolean('${atp.logging.feignclient.headers:false}')}")
    private Boolean logFeignHeaders;

    /**
     * Log Headers of Requests/Responses processed by RestTemplates (true) or not.
     */
    @Getter
    @Value("#{new Boolean('${atp.logging.resttemplate.headers:false}')}")
    private Boolean logRestTemplateHeaders;

    /**
     * Ignore Headers List for Controllers Logging.
     */
    @Value("#{'${atp.logging.controller.headers.ignore:Authorization}'.split(' ')}")
    private List<String> ignoreControllerHeaders;

    /**
     * Ignore Headers List for Feign Clients Logging.
     */
    @Value("#{'${atp.logging.feignclient.headers.ignore:Authorization}'.split(' ')}")
    private List<String> ignoreFeignHeaders;

    /**
     * Ignore Headers List for RestTemplates Logging.
     */
    @Value("#{'${atp.logging.resttemplate.headers.ignore:Authorization}'.split(' ')}")
    private List<String> ignoreRestTemplateHeaders;

    /**
     * Ignore URIs List for Controllers Logging.
     */
    @Value("#{'${atp.logging.controller.uri.ignore:/sse/.* /deployment/readiness /deployment/liveness}'.split(' ')}")
    private List<String> ignoreUriList;

    /**
     * 'Ignore Headers' Patterns List for Controllers Logging.
     */
    private List<Pattern> ignoreControllerHeaderPatterns;

    /**
     * 'Ignore Headers' Patterns List for Feign Clients Logging.
     */
    private List<Pattern> ignoreFeignHeaderPatterns;

    /**
     * 'Ignore Headers' Patterns List for RestTemplates Logging.
     */
    private List<Pattern> ignoreRestTemplateHeaderPatterns;

    /**
     * 'Ignore URIs' Patterns List for Controllers Logging.
     */
    private List<Pattern> ignoreUriListPatterns;

    /**
     * Returns list of compiled regex patterns to ignore matched http headers for RestController.
     * @return list of Pattern objects
     */
    public List<Pattern> getIgnoreControllerHeaders() {
        if (isNull(ignoreControllerHeaderPatterns)) {
            ignoreControllerHeaderPatterns = compile(ignoreControllerHeaders);
        }
        return ignoreControllerHeaderPatterns;
    }

    /**
     * Returns list of compiled regex patterns to ignore matched http headers for Feign client.
     * @return list of Pattern objects
     */
    public List<Pattern> getIgnoreFeignHeaderPatterns() {
        if (isNull(ignoreFeignHeaderPatterns)) {
            ignoreFeignHeaderPatterns = compile(ignoreFeignHeaders);
        }
        return ignoreFeignHeaderPatterns;
    }

    /**
     * Returns list of compiled regex patterns to ignore matched http headers for RestTemplate client.
     * @return list of Pattern objects
     */
    public List<Pattern> getIgnoreRestTemplateHeaderPatterns() {
        if (isNull(ignoreRestTemplateHeaderPatterns)) {
            ignoreRestTemplateHeaderPatterns = compile(ignoreRestTemplateHeaders);
        }
        return ignoreRestTemplateHeaderPatterns;
    }

    /**
     * Returns list of compiled regex patterns to ignore matched URI.
     * @return list of Pattern objects
     */
    public List<Pattern> getIgnoreUriListPatterns() {
        if (isNull(ignoreUriListPatterns)) {
            ignoreUriListPatterns = compile(ignoreUriList);
        }
        return ignoreUriListPatterns;
    }

    private List<Pattern> compile(final List<String> ignoreHeaders) {
        return ignoreHeaders
            .stream()
            .filter(StringUtils::isNotEmpty)
            .map(Pattern::compile)
            .collect(toList());
    }
}
