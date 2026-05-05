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

package sonia.scm.repository.spi;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.time.Instant;
import java.util.Locale;

/**
 *
 * @since 1.17
 */
@Getter
@Setter
public final class LogCommandRequest implements Serializable, Resetable, Cloneable {

  private static final long serialVersionUID = 8759598040394428649L;

  private String branch;

  private String endChangeset;

  private int pagingLimit = 20;

  private int pagingStart = 0;

  private String path;

  private String startChangeset;

  private String ancestorChangeset;

  private String partOfMessage;

  private String person;

  private Instant before;

  private Instant after;

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final LogCommandRequest other = (LogCommandRequest) obj;

    //J-
    return Objects.equal(startChangeset, other.startChangeset)
      && Objects.equal(endChangeset, other.endChangeset)
      && Objects.equal(pagingStart, other.pagingStart)
      && Objects.equal(pagingLimit, other.pagingLimit)
      && Objects.equal(path, other.path)
      && Objects.equal(branch, other.branch)
      && Objects.equal(ancestorChangeset, other.ancestorChangeset)
      && Objects.equal(partOfMessage, other.partOfMessage)
      && Objects.equal(person, other.person)
      && Objects.equal(before, other.before)
      && Objects.equal(after, other.after);
    //J+
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
      startChangeset, endChangeset, pagingStart, pagingLimit,
      path, branch, ancestorChangeset, partOfMessage, person,
      before, after
    );
  }

  @SneakyThrows
  public LogCommandRequest clone() {
    return (LogCommandRequest) super.clone();
  }

  @Override
  public void reset() {
    startChangeset = null;
    endChangeset = null;
    pagingStart = 0;
    pagingLimit = 20;
    path = null;
    branch = null;
    ancestorChangeset = null;
    partOfMessage = null;
    person = null;
    before = null;
    after = null;
  }

  @Override
  public String toString() {
    //J-
    return MoreObjects.toStringHelper(this)
      .add("startChangeset", startChangeset)
      .add("endChangeset", endChangeset)
      .add("pagingStart", pagingStart)
      .add("pagingLimit", pagingLimit)
      .add("path", path)
      .add("branch", branch)
      .add("ancestorChangeset", ancestorChangeset)
      .add("partOfMessage", partOfMessage)
      .add("person", person)
      .add("before", before)
      .add("after", after)
      .toString();
    //J+
  }

  public boolean isPagingUnlimited() {
    return pagingLimit < 0;
  }

  public void setPartOfMessage(String partOfMessage) {
    if (partOfMessage != null) {
      partOfMessage = partOfMessage.toLowerCase(Locale.ENGLISH);
    }

    this.partOfMessage = partOfMessage;
  }

  public void setPerson(String person) {
    if (person != null) {
      person = person.toLowerCase(Locale.ENGLISH);
    }

    this.person = person;
  }
}
