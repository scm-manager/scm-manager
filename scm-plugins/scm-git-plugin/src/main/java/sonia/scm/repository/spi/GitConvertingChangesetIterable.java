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

import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;

import java.util.Iterator;

class GitConvertingChangesetIterable implements Iterable<Changeset> {

  private final Iterable<RevCommit> commitIterable;
  private final GitChangesetConverter converter;

  GitConvertingChangesetIterable(Iterable<RevCommit> commitIterable,
                                         GitChangesetConverter converter) {
    this.commitIterable = commitIterable;
    this.converter = converter;
  }

  @Override
  public Iterator<Changeset> iterator() {
    return new ConvertingChangesetIterator(commitIterable.iterator());
  }

  class ConvertingChangesetIterator implements Iterator<Changeset> {

    private final Iterator<RevCommit> commitIterator;

    private ConvertingChangesetIterator(Iterator<RevCommit> commitIterator) {
      this.commitIterator = commitIterator;
    }

    @Override
    public boolean hasNext() {
      return commitIterator.hasNext();
    }

    @Override
    public Changeset next() {
      return converter.createChangeset(commitIterator.next());
    }
  }
}
