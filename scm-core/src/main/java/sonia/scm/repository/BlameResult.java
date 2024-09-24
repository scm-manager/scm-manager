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
import lombok.ToString;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Changeset information by line for a given file.
 *
 * @since 1.8
 */
@EqualsAndHashCode
@ToString
public class BlameResult implements Serializable, Iterable<BlameLine> {

  private static final long serialVersionUID = -8606237881465520606L;

  private List<BlameLine> blameLines;
  private int total;

  public BlameResult(List<BlameLine> blameLines) {
    this(blameLines.size(), blameLines);
  }

  public BlameResult(int total, List<BlameLine> blameLines) {
    this.total = total;
    this.blameLines = blameLines;
  }

  @Override
  public Iterator<BlameLine> iterator() {
    return getBlameLines().iterator();
  }

  public List<BlameLine> getBlameLines() {
    if (blameLines == null) {
      blameLines = Lists.newArrayList();
    }
    return blameLines;
  }

  public BlameLine getLine(int i) {
    return blameLines.get(i);
  }

  public int getTotal() {
    return total;
  }
}
