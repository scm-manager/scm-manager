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
 * Represents all branches of a repository.
 *
 * @since 1.18
 */
@EqualsAndHashCode
@ToString
@Setter
public final class Branches implements Iterable<Branch> {

  private List<Branch> branches;

  public Branches() {
  }

  public Branches(Branch... branches) {
    this.branches = Lists.newArrayList(branches);
  }

  public Branches(List<Branch> branches) {
    this.branches = branches;
  }

  @Override
  public Iterator<Branch> iterator() {
    return getBranches().iterator();
  }

  public List<Branch> getBranches() {
    if (branches == null) {
      branches = Lists.newArrayList();
    }

    return branches;
  }

  public void setBranches(List<Branch> branches) {
    this.branches = branches;
  }
}
