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

package sonia.scm.repository.spi;

import com.google.common.collect.Lists;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.InternalRepositoryException;

import java.util.List;
import java.util.Optional;

public class SvnChangesetsCommand extends AbstractSvnCommand implements ChangesetsCommand {

  SvnChangesetsCommand(SvnContext context) {
    super(context);
  }

  @Override
  public Iterable<Changeset> getChangesets(ChangesetsCommandRequest request) {
    try {
      SVNRepository repo = open();
      long startRev =  repo.getLatestRevision();
      // We ignore the changeset 0, because it doesn't have any values like author or description
      long endRev = 1;
      final List<Changeset> changesets = Lists.newArrayList();

      repo.log(null, startRev, endRev, true, true, new ChangesetCollector(changesets));
      return changesets;
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not open repository: " + repository.getNamespaceAndName(), ex);
    }
  }

  @Override
  public Optional<Changeset> getLatestChangeset() {
    try {
      SVNRepository repo = open();
      long latestRevision =  repo.getLatestRevision();
      final List<Changeset> changesets = Lists.newArrayList();

      repo.log(null, latestRevision, latestRevision, true, true, new ChangesetCollector(changesets));

      if (!changesets.isEmpty()) {
        return Optional.of(changesets.get(0));
      }
      return Optional.empty();
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not open repository: " + repository.getNamespaceAndName(), ex);
    }
  }
}
