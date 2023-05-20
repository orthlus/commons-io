/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;

/**
 * Abstracts building a typed instance of {@code T}.
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @since 2.12.0
 */
public abstract class AbstractStreamBuilder<T, B extends AbstractStreamBuilder<T, B>> extends AbstractOriginSupplier<T, B> {

    private static final OpenOption[] DEFAULT_OPEN_OPTIONS = PathUtils.EMPTY_OPEN_OPTION_ARRAY;

    /**
     * The buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    private int bufferSize = IOUtils.DEFAULT_BUFFER_SIZE;

    /**
     * The buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    private int bufferSizeDefault = IOUtils.DEFAULT_BUFFER_SIZE;

    /**
     * The Charset, defaults to {@link Charset#defaultCharset()}.
     */
    private Charset charset = Charset.defaultCharset();

    /**
     * The Charset, defaults to {@link Charset#defaultCharset()}.
     */
    private Charset charsetDefault = Charset.defaultCharset();

    private OpenOption[] openOptions = DEFAULT_OPEN_OPTIONS;

    /**
     * Gets the buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     *
     * @return the buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    protected int getBufferSize() {
        return bufferSize;
    }

    /**
     * Gets the buffer size default, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     *
     * @return the buffer size default, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    protected int getBufferSizeDefault() {
        return bufferSizeDefault;
    }

    /**
     * Gets a CharSequence from the origin with a Charset.
     *
     * @return An input stream
     * @throws IOException                   if an I/O error occurs.
     * @throws UnsupportedOperationException if the origin cannot be converted to a CharSequence.
     * @see AbstractOrigin#getCharSequence(Charset)
     * @since 2.13.0
     */
    protected CharSequence getCharSequence() throws IOException {
        return getOrigin().getCharSequence(getCharset());
    }

    /**
     * Gets the Charset, defaults to {@link Charset#defaultCharset()}.
     *
     * @return the Charset, defaults to {@link Charset#defaultCharset()}.
     */
    protected Charset getCharset() {
        return charset;
    }

    /**
     * Gets the Charset default, defaults to {@link Charset#defaultCharset()}.
     *
     * @return the Charset default, defaults to {@link Charset#defaultCharset()}.
     */
    protected Charset getCharsetDefault() {
        return charsetDefault;
    }

    /**
     * Gets an input stream from the origin with open options.
     *
     * @return An input stream
     * @throws IOException                   if an I/O error occurs.
     * @throws UnsupportedOperationException if the origin cannot be converted to an InputStream.
     * @see AbstractOrigin#getInputStream(OpenOption...)
     * @since 2.13.0
     */
    protected InputStream getInputStream() throws IOException {
        return getOrigin().getInputStream(getOpenOptions());
    }

    protected OpenOption[] getOpenOptions() {
        return openOptions;
    }

    /**
     * Gets an output stream from the origin with open options.
     *
     * @return An input stream
     * @throws IOException                   if an I/O error occurs.
     * @throws UnsupportedOperationException if the origin cannot be converted to an OututStream.
     * @see AbstractOrigin#getOutputStream(OpenOption...)
     * @since 2.13.0
     */
    protected OutputStream getOutputStream() throws IOException {
        return getOrigin().getOutputStream(getOpenOptions());
    }

    /**
     * Gets an writer from the origin with open options.
     *
     * @return An writer.
     * @throws IOException                   if an I/O error occurs.
     * @throws UnsupportedOperationException if the origin cannot be converted to a Writer.
     * @see AbstractOrigin#getOutputStream(OpenOption...)
     * @since 2.13.0
     */
    protected Writer getWriter() throws IOException {
        return getOrigin().getWriter(getCharset(), getOpenOptions());
    }

    /**
     * Sets the buffer size.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSize the buffer size.
     * @return this.
     */
    public B setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize >= 0 ? bufferSize : bufferSizeDefault;
        return asThis();
    }

    /**
     * Sets the buffer size.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSize the buffer size, null resets to the default.
     * @return this.
     */
    public B setBufferSize(final Integer bufferSize) {
        setBufferSize(bufferSize != null ? bufferSize : bufferSizeDefault);
        return asThis();
    }

    /**
     * Sets the buffer size for subclasses to initialize.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSizeDefault the buffer size, null resets to the default.
     * @return this.
     */
    protected B setBufferSizeDefault(final int bufferSizeDefault) {
        this.bufferSizeDefault = bufferSizeDefault;
        return asThis();
    }

    /**
     * Sets the Charset.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param charset the Charset, null resets to the default.
     * @return this.
     */
    public B setCharset(final Charset charset) {
        this.charset = Charsets.toCharset(charset, charsetDefault);
        return asThis();
    }

    /**
     * Sets the Charset.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param charset the Charset name, null resets to the default.
     * @return this.
     */
    public B setCharset(final String charset) {
        return setCharset(Charsets.toCharset(charset, charsetDefault));
    }

    /**
     * Sets the Charset default for subclasses to initialize.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param defaultCharset the Charset name, null resets to the default.
     * @return this.
     */
    protected B setCharsetDefault(final Charset defaultCharset) {
        this.charsetDefault = defaultCharset;
        return asThis();
    }

    /**
     * Sets the OpenOption[].
     * <p>
     * Normally used with InputStream, OutputStream, and Writer.
     * </p>
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param openOptions the OpenOption[] name, null resets to the default.
     * @return this.
     * @since 2.13.0
     * @see #setInputStream(InputStream)
     * @see #setOutputStream(OutputStream)
     * @see #setWriter(Writer)
     */
    public B setOpenOptions(final OpenOption... openOptions) {
        this.openOptions = openOptions != null ? openOptions : DEFAULT_OPEN_OPTIONS;
        return asThis();
    }
}
