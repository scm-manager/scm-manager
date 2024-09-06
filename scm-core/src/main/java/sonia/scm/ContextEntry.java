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
import sonia.scm.repository.Repository;
import sonia.scm.util.AssertUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ContextEntry implements Serializable {
  private final String type;
  private final String id;

  ContextEntry(Class<?> type, String id) {
    this(type.getSimpleName(), id);
  }

  ContextEntry(String type, String id) {
    AssertUtil.assertIsNotEmpty(type);
    AssertUtil.assertIsNotEmpty(id);
    this.type = type;
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }


  public static class ContextBuilder {
    private final List<ContextEntry> context = new LinkedList<>();

    public static List<ContextEntry> noContext() {
      return new ContextBuilder().build();
    }

    public static List<ContextEntry> only(String type, String id) {
      return new ContextBuilder().in(type, id).build();
    }

    public static ContextBuilder entity(Repository repository) {
      return new ContextBuilder().in(repository.getNamespaceAndName());
    }

    public static ContextBuilder entity(NamespaceAndName namespaceAndName) {
      return new ContextBuilder().in(Repository.class, namespaceAndName.logString());
    }

    public static ContextBuilder entity(Class type, String id) {
      return new ContextBuilder().in(type, id);
    }

    public static ContextBuilder entity(String type, String id) {
      return new ContextBuilder().in(type, id);
    }

    public ContextBuilder in(Repository repository) {
      return in(repository.getNamespaceAndName());
    }

    public ContextBuilder in(NamespaceAndName namespaceAndName) {
      return this.in(Repository.class, namespaceAndName.logString());
    }

    public ContextBuilder in(Class<?> type, String id) {
      context.add(new ContextEntry(type, id));
      return this;
    }

    public ContextBuilder in(String type, String id) {
      context.add(new ContextEntry(type, id));
      return this;
    }

    public List<ContextEntry> build() {
      return Collections.unmodifiableList(context);
    }
  }
}
