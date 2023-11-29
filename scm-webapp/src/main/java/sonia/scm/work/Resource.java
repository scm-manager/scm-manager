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

package sonia.scm.work;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
final class Resource implements Serializable {

  private final String name;
  @Nullable
  private final String id;

  Resource(String name) {
    this.name = name;
    this.id = null;
  }

  Resource(String name, @Nullable String id) {
    this.name = name;
    this.id = id;
  }

  boolean isBlockedBy(Resource resource) {
    if (name.equals(resource.name)) {
      if (id != null && resource.id != null) {
        return id.equals(resource.id);
      }
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    if (id != null) {
      return name + ":" + id;
    }
    return name;
  }
}
