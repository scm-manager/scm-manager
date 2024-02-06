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

import java.util.Iterator;
import java.util.List;

/**
 * Represents all tags of a repository.
 *
 * @since 1.18
 */
@EqualsAndHashCode
@ToString
@Setter
public final class Tags implements Iterable<Tag> {

  private List<Tag> tags;

  public Tags() {
  }

  public Tags(List<Tag> tags) {
    this.tags = tags;
  }

  @Override
  public Iterator<Tag> iterator() {
    return getTags().iterator();
  }

  /**
   * Returns the {@link Tag} with the given name or null.
   */
  public Tag getTagByName(String name) {
    Tag tag = null;

    for (Tag t : getTags()) {
      if (name.equals(t.getName())) {
        tag = t;

        break;
      }
    }

    return tag;
  }

  /**
   * Returns the {@link Tag} with the given revision or null.
   */
  public Tag getTagByRevision(String revision) {
    Tag tag = null;

    for (Tag t : getTags()) {
      if (revision.equals(t.getRevision())) {
        tag = t;

        break;
      }
    }

    return tag;
  }

  /**
   * Returns all tags of a repository.
   */
  public List<Tag> getTags() {
    if (tags == null) {
      tags = Lists.newArrayList();
    }

    return tags;
  }
}
