/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

public abstract class ExceptionWithContext extends RuntimeException {

  private static final long serialVersionUID = 4327413456580409224L;

  private final List<ContextEntry> context;
  private final List<AdditionalMessage> additionalMessages;

  protected ExceptionWithContext(List<ContextEntry> context, String message) {
    this(context, null, message);
  }

  protected ExceptionWithContext(List<ContextEntry> context, List<AdditionalMessage> additionalMessages, String message) {
    super(message);
    this.context = context;
    this.additionalMessages = additionalMessages;
  }

  protected ExceptionWithContext(List<ContextEntry> context, String message, Exception cause) {
    this(context, null, message, cause);
  }

  protected ExceptionWithContext(List<ContextEntry> context, List<AdditionalMessage> additionalMessages, String message, Exception cause) {
    super(message, cause);
    this.context = context;
    this.additionalMessages = additionalMessages;
  }

  public List<ContextEntry> getContext() {
    return unmodifiableList(context);
  }

  public abstract String getCode();

  /**
   * Returns an url which gives more information about the exception or an empty optional.
   * The method returns an empty optional by default and can be overwritten.
   *
   * @since 2.5.0
   */
  public Optional<String> getUrl() {
    return Optional.empty();
  }

  public List<AdditionalMessage> getAdditionalMessages() {
    return additionalMessages;
  }

  public static class AdditionalMessage implements Serializable {
    private final String key;
    private final String message;

    public AdditionalMessage(String key, String message) {
      this.key = key;
      this.message = message;
    }

    public String getKey() {
      return key;
    }

    public String getMessage() {
      return message;
    }
  }
}
