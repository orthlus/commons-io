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

package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

/**
 * Implements an {@link InputStream} to read from String, StringBuffer, StringBuilder or CharBuffer.
 * <p>
 * <strong>Note:</strong> Supports {@link #mark(int)} and {@link #reset()}.
 * </p>
 *
 * @since 2.2
 */
public class CharSequenceInputStream extends InputStream {

    /**
     * Builds a new {@link CharSequenceInputStream} instance.
     * <p>
     * For example:
     * </p>
     *
     * <pre>{@code
     * CharSequenceInputStream s = CharSequenceInputStream.builder()
     *   .setBufferSize(8192)
     *   .setCharSequence("String")
     *   .setCharsetEncoder(Charset.defaultCharset())
     *   .get();}
     * </pre>
     *
     * @since 2.13.0
     */
    public static class Builder extends AbstractStreamBuilder<CharSequenceInputStream, Builder> {

        /**
         * Constructs a new instance.
         * <p>
         * This builder use the aspects the CharSequence, buffer size, and Charset.
         * </p>
         *
         * @return a new instance.
         * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
         */
        @Override
        public CharSequenceInputStream get() {
            return Uncheck.get(() -> new CharSequenceInputStream(getCharSequence(), getCharset(), getBufferSize()));
        }

    }

    private static final int NO_MARK = -1;

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private final CharsetEncoder charsetEncoder;
    private final CharBuffer cBuf;
    private final ByteBuffer bBuf;

    private int cBufMark; // position in cBuf
    private int bBufMark; // position in bBuf

    /**
     * Constructs a new instance with a buffer size of {@link IOUtils#DEFAULT_BUFFER_SIZE}.
     *
     * @param cs the input character sequence.
     * @param charset the character set name to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     * @deprecated Use {@link #builder()} and {@link Builder#get()}
     */
    @Deprecated
    public CharSequenceInputStream(final CharSequence cs, final Charset charset) {
        this(cs, charset, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param cs the input character sequence.
     * @param charset the character set name to use, null maps to the default Charset.
     * @param bufferSize the buffer size to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     * @deprecated Use {@link #builder()} and {@link Builder#get()}
     */
    @Deprecated
    public CharSequenceInputStream(final CharSequence cs, final Charset charset, final int bufferSize) {
        // @formatter:off
        this.charsetEncoder = Charsets.toCharset(charset).newEncoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);
        // @formatter:on
        // Ensure that buffer is long enough to hold a complete character
        this.bBuf = ByteBuffer.allocate(ReaderInputStream.checkMinBufferSize(charsetEncoder, bufferSize));
        this.bBuf.flip();
        this.cBuf = CharBuffer.wrap(cs);
        this.cBufMark = NO_MARK;
        this.bBufMark = NO_MARK;
    }

    /**
     * Constructs a new instance with a buffer size of {@link IOUtils#DEFAULT_BUFFER_SIZE}.
     *
     * @param cs the input character sequence.
     * @param charset the character set name to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     * @deprecated Use {@link #builder()} and {@link Builder#get()}
     */
    @Deprecated
    public CharSequenceInputStream(final CharSequence cs, final String charset) {
        this(cs, charset, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param cs the input character sequence.
     * @param charset the character set name to use, null maps to the default Charset.
     * @param bufferSize the buffer size to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     * @deprecated Use {@link #builder()} and {@link Builder#get()}
     */
    @Deprecated
    public CharSequenceInputStream(final CharSequence cs, final String charset, final int bufferSize) {
        this(cs, Charsets.toCharset(charset), bufferSize);
    }

    /**
     * Return an estimate of the number of bytes remaining in the byte stream.
     * @return the count of bytes that can be read without blocking (or returning EOF).
     *
     * @throws IOException if an error occurs (probably not possible).
     */
    @Override
    public int available() throws IOException {
        // The cached entries are in bBuf; since encoding always creates at least one byte
        // per character, we can add the two to get a better estimate (e.g. if bBuf is empty)
        // Note that the previous implementation (2.4) could return zero even though there were
        // encoded bytes still available.
        return this.bBuf.remaining() + this.cBuf.remaining();
    }

    @Override
    public void close() throws IOException {
        // noop
    }

    /**
     * Fills the byte output buffer from the input char buffer.
     *
     * @throws CharacterCodingException
     *             an error encoding data.
     */
    private void fillBuffer() throws CharacterCodingException {
        this.bBuf.compact();
        final CoderResult result = this.charsetEncoder.encode(this.cBuf, this.bBuf, true);
        if (result.isError()) {
            result.throwException();
        }
        this.bBuf.flip();
    }

    /**
     * Gets the CharsetEncoder.
     *
     * @return the CharsetEncoder.
     */
    CharsetEncoder getCharsetEncoder() {
        return charsetEncoder;
    }

    /**
     * {@inheritDoc}
     * @param readlimit max read limit (ignored).
     */
    @Override
    public synchronized void mark(final int readlimit) {
        this.cBufMark = this.cBuf.position();
        this.bBufMark = this.bBuf.position();
        this.cBuf.mark();
        this.bBuf.mark();
        // It would be nice to be able to use mark & reset on the cBuf and bBuf;
        // however the bBuf is re-used so that won't work
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        for (;;) {
            if (this.bBuf.hasRemaining()) {
                return this.bBuf.get() & 0xFF;
            }
            fillBuffer();
            if (!this.bBuf.hasRemaining() && !this.cBuf.hasRemaining()) {
                return EOF;
            }
        }
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] array, int off, int len) throws IOException {
        Objects.requireNonNull(array, "array");
        if (len < 0 || off + len > array.length) {
            throw new IndexOutOfBoundsException("Array Size=" + array.length + ", offset=" + off + ", length=" + len);
        }
        if (len == 0) {
            return 0; // must return 0 for zero length read
        }
        if (!this.bBuf.hasRemaining() && !this.cBuf.hasRemaining()) {
            return EOF;
        }
        int bytesRead = 0;
        while (len > 0) {
            if (this.bBuf.hasRemaining()) {
                final int chunk = Math.min(this.bBuf.remaining(), len);
                this.bBuf.get(array, off, chunk);
                off += chunk;
                len -= chunk;
                bytesRead += chunk;
            } else {
                fillBuffer();
                if (!this.bBuf.hasRemaining() && !this.cBuf.hasRemaining()) {
                    break;
                }
            }
        }
        return bytesRead == 0 && !this.cBuf.hasRemaining() ? EOF : bytesRead;
    }

    @Override
    public synchronized void reset() throws IOException {
        //
        // This is not the most efficient implementation, as it re-encodes from the beginning.
        //
        // Since the bBuf is re-used, in general it's necessary to re-encode the data.
        //
        // It should be possible to apply some optimizations however:
        // + use mark/reset on the cBuf and bBuf. This would only work if the buffer had not been (re)filled since
        // the mark. The code would have to catch InvalidMarkException - does not seem possible to check if mark is
        // valid otherwise. + Try saving the state of the cBuf before each fillBuffer; it might be possible to
        // restart from there.
        //
        if (this.cBufMark != NO_MARK) {
            // if cBuf is at 0, we have not started reading anything, so skip re-encoding
            if (this.cBuf.position() != 0) {
                this.charsetEncoder.reset();
                this.cBuf.rewind();
                this.bBuf.rewind();
                this.bBuf.limit(0); // rewind does not clear the buffer
                while(this.cBuf.position() < this.cBufMark) {
                    this.bBuf.rewind(); // empty the buffer (we only refill when empty during normal processing)
                    this.bBuf.limit(0);
                    fillBuffer();
                }
            }
            if (this.cBuf.position() != this.cBufMark) {
                throw new IllegalStateException("Unexpected CharBuffer position: actual=" + cBuf.position() + " " +
                        "expected=" + this.cBufMark);
            }
            this.bBuf.position(this.bBufMark);
            this.cBufMark = NO_MARK;
            this.bBufMark = NO_MARK;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        //
        // This could be made more efficient by using position to skip within the current buffer.
        //
        long skipped = 0;
        while (n > 0 && available() > 0) {
            this.read();
            n--;
            skipped++;
        }
        return skipped;
    }

}
