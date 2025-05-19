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

package org.qubership.atp.common.logging.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StreamUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonHttpRequestWrapper extends HttpServletRequestWrapper {

    /**
     * Request body.
     */
    private final byte[] actualRequestBody;

    /**
     * Constructor; Also it prepares request body.
     *
     * @param request HttpServletRequest object
     * @throws IOException in case input stream reading exceptions.
     */
    public CommonHttpRequestWrapper(final HttpServletRequest request) throws IOException {
        super(request);
        this.actualRequestBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    /**
     * Get request content.
     *
     * @return byte[] actualRequestBody value.
     */
    public byte[] getContent() {
        return actualRequestBody;
    }

    /**
     * Get Input Stream.
     *
     * @return new CachedServletInputStream for actualRequestBody.
     */
    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream(this.actualRequestBody);
    }

    /**
     * Get Reader of actualRequestBody.
     *
     * @return new BufferedReader of ByteArrayInputStream of actualRequestBody.
     */
    @Override
    public BufferedReader getReader() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.actualRequestBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    private class CachedServletInputStream extends ServletInputStream {
        private InputStream cachedInputStream;

        /**
         * Constructor.
         *
         * @param cachedBody byte[] body.
         */
        public CachedServletInputStream(final byte[] cachedBody) {
            this.cachedInputStream = new ByteArrayInputStream(cachedBody);
        }

        /**
         * Check if Input Stream is finished or not.
         *
         * @return true if input stream is finished; otherwise false.
         */
        @Override
        public boolean isFinished() {
            try {
                return cachedInputStream.available() == 0;
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
            return false;
        }

        /**
         * Check if Input Stream is ready or not.
         *
         * @return Always returns true.
         */
        @Override
        public boolean isReady() {
            return true;
        }

        /**
         * Set ReadListener. Currently unsupported.
         *
         * @param readListener ReadListener object.
         */
        @Override
        public void setReadListener(final ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        /**
         * Read from Input Stream.
         *
         * @return the next byte of data, or -1 if the end of the stream is reached
         * @throws IOException if an I/O error occurs.
         */
        @Override
        public int read() throws IOException {
            return cachedInputStream.read();
        }
    }
}
