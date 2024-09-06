/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
