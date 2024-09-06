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

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class ConcurrentModificationException extends ExceptionWithContext {

  private static final String CODE = "2wR7UzpPG1";

  public ConcurrentModificationException(Class<?> type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  public ConcurrentModificationException(String type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  public ConcurrentModificationException(List<ContextEntry> context) {
    super(context, createMessage(context));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(List<ContextEntry> context) {
    return context.stream()
      .map(c -> c.getType().toLowerCase() + " with id " + c.getId())
      .collect(joining(" in ", "", " has been modified concurrently"));
  }
}

