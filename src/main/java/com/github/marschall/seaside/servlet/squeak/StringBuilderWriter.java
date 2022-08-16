package com.github.marschall.seaside.servlet.squeak;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * A {@link Writer} writing to a {@link StringBuilder}.
 * <p>
 * Avoids the locking / {@link StringBuilder} of {@link StringWriter}.
 * 
 * @see Reader#transferTo(Writer)
 */
final class StringBuilderWriter extends Writer {
  
  private final StringBuilder buffer;

  StringBuilderWriter() {
    this.buffer = new StringBuilder();
  }

  @Override
  public void write(int c) {
    this.buffer.append((char) c);
  }

  @Override
  public void write(char[] cbuf) {
    this.buffer.append(cbuf);
  }

  @Override
  public void write(String str) {
    this.buffer.append(str);
  }

  @Override
  public void write(String str, int off, int len) {
    this.buffer.append(str, off, off + len);
  }

  @Override
  public Writer append(CharSequence csq) {
    this.buffer.append(csq);
    return this;
  }

  @Override
  public Writer append(CharSequence csq, int start, int end) {
    this.buffer.append(csq, start, end);
    return this;
  }

  @Override
  public Writer append(char c) {
    this.buffer.append(c);
    return this;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    this.buffer.append(cbuf, off, len);
  }

  @Override
  public void flush() {
    // ignore

  }

  @Override
  public void close() {
    // ignore

  }
  
  @Override
  public String toString() {
    return this.buffer.toString();
  }

}
