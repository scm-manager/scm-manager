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

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class NotUniqueRevisionException extends ExceptionWithContext {

  private static final long serialVersionUID = 1710455380886499111L;

  private static final String CODE = "9tSzgRvIu1";

  public NotUniqueRevisionException(Class<?> type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  public NotUniqueRevisionException(String type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  public static NotUniqueRevisionException notUniqueIdentifier(ContextEntry.ContextBuilder contextBuilder) {
    return new NotUniqueRevisionException(contextBuilder.build());
  }

  private NotUniqueRevisionException(List<ContextEntry> context) {
    super(context, createMessage(context));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(List<ContextEntry> context) {
    return context.stream()
      .map(c -> c.getType().toLowerCase() + " with id " + c.getId())
      .collect(joining(" in ", "Not unique identifier ", ""));
  }
}
