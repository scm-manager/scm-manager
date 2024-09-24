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
