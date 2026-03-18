/*
 * # Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.atp.common.logging.utils;

import org.apache.commons.lang3.StringUtils;

public enum ContentType {

    /**
     * Constant for application/json mime-type.
     */
    APPLICATION_JSON("application/json", true),

    /**
     * Constant for application/xml mime-type.
     */
    APPLICATION_XML("application/xml", true),

    /**
     * Constant for text/csv mime-type.
     */
    TEXT_CSV("text/csv", true),

    /**
     * Constant for text/xml mime-type.
     */
    TEXT_XML("text/xml", true),

    /**
     * Constant for text/html mime-type.
     */
    TEXT_HTML("text/html", true),

    /**
     * Constant for text/plain mime-type.
     */
    TEXT_PAIN("text/plain", true),

    /**
     * Constant for undefined mime-type (logging is disabled).
     */
    UNDEFINED("undefined", false);

    /**
     * String mime-type canonical name.
     */
    private final String mimeType;

    /**
     * Flag if logging is enabled or not.
     */
    @lombok.Getter
    private final boolean loggingAllowed;

    @SuppressWarnings("checkstyle:HiddenField")
    ContentType(final String mimeType, final boolean loggingAllowed) {
        this.mimeType = mimeType;
        this.loggingAllowed = loggingAllowed;
    }

    /**
     * Get content type.
     *
     * @param value type as string
     * @return ContentType
     */
    public static ContentType getContentType(final String value) {
        for (ContentType type : ContentType.values()) {
            if (type.mimeType.equals(value) || !StringUtils.isEmpty(value)
                    && (type.mimeType.matches(".*?\\b" + value + "\\b.*?")
                    || value.matches(".*?\\b" + type.mimeType + "\\b.*?"))) {
                return type;
            }
        }
        return ContentType.UNDEFINED;
    }

}
