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

package sonia.scm.auditlog;

import lombok.Getter;

import java.util.Set;

import static java.util.Collections.emptySet;

@Getter
public class EntryCreationContext<T> {
  private final T object;
  private final T oldObject;
  private final String entity;
  private final Set<String> additionalLabels;

  public EntryCreationContext(T object, T oldObject) {
    this(object, oldObject, "", emptySet());
  }

  public EntryCreationContext(T object, T oldObject, Set<String> additionalLabels) {
    this(object, oldObject, "", additionalLabels);
  }

  public EntryCreationContext(T object, T oldObject, String entity, Set<String>  additionalLabels) {
    this.object = object;
    this.oldObject = oldObject;
    this.entity = entity;
    this.additionalLabels = additionalLabels;
  }
}
