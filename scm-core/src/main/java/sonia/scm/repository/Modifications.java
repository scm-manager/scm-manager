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

package sonia.scm.repository;

import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode
@ToString
@Setter
public class Modifications implements Serializable {

  private static final long serialVersionUID = -8902033326668658140L;
  private String revision;

  /**
   * lists of changed files
   */
  private List<String> added;
  private List<String> modified;
  private List<String> removed;

  public Modifications() {
  }

  ;

  public Modifications(List<String> added) {
    this(added, null, null);
  }

  public Modifications(List<String> added, List<String> modified) {
    this(added, modified, null);
  }

  public Modifications(List<String> added, List<String> modified, List<String> removed) {
    this.added = added;
    this.modified = modified;
    this.removed = removed;
  }

  public List<String> getAdded() {
    if (added == null) {
      added = Lists.newArrayList();
    }

    return added;
  }

  public List<String> getModified() {
    if (modified == null) {
      modified = Lists.newArrayList();
    }

    return modified;
  }

  public List<String> getRemoved() {
    if (removed == null) {
      removed = Lists.newArrayList();
    }

    return removed;
  }

  public String getRevision() {
    return revision;
  }
}
