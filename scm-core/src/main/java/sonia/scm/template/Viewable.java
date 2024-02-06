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
    
package sonia.scm.template;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A viewable holds the path to a template and the context object which is used to render the template. Viewables can
 * be used as return type of jax-rs resources.
 * 
 * @since 2.0.0
 */
public final class Viewable {
  
  private final String path;
  private final Object context;

  public Viewable(String path, Object context) {
    this.path = path;
    this.context = context;
  }

  public String getPath() {
    return path;
  }

  public Object getContext() {
    return context;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(path, context);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Viewable other = (Viewable) obj;
    return !Objects.equal(this.path, other.path)
      && Objects.equal(this.context, other.context);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("path", path)
            .add("context", context)
            .toString();
  }
  
}
