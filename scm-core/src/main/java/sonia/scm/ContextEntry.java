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

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.util.AssertUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ContextEntry {
  private final String type;
  private final String id;

  ContextEntry(Class type, String id) {
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

    public ContextBuilder in(Class type, String id) {
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
