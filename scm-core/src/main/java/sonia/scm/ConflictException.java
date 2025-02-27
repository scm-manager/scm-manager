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

import sonia.scm.repository.NamespaceAndName;

import java.util.Collection;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class ConflictException extends ExceptionWithContext {
  private static final String CODE = "7XUd94Iwo1";

  public ConflictException(NamespaceAndName namespaceAndName, Collection<String> conflictingFiles) {
    super(
      createContext(namespaceAndName, conflictingFiles),
      "conflict"
    );
  }

  private static List<ContextEntry> createContext(NamespaceAndName namespaceAndName, Collection<String> conflictingFiles) {
    return entity("files", String.join(", ", conflictingFiles))
      .in(namespaceAndName)
      .build();
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
