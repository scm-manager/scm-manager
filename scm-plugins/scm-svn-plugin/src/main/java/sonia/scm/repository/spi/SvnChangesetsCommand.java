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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.InternalRepositoryException;

import java.util.List;
import java.util.Optional;

@Slf4j
public class SvnChangesetsCommand extends AbstractSvnCommand implements ChangesetsCommand {

  SvnChangesetsCommand(SvnContext context) {
    super(context);
  }

  /**
   * We ignore the changeset 0, because it doesn't have any values like author or description.
   *
   * @param request {@link ChangesetsCommandRequest}
   * @return iterable of {@link Changeset}
   */
  @Override
  public Iterable<Changeset> getChangesets(ChangesetsCommandRequest request) {
    try {
      log.debug("trying to get SVN changesets in repository {}", context.getRepository());
      SVNRepository repo = open();
      long startRev = repo.getLatestRevision();
      if (startRev > 0) {
        long endRev = 1;
        final List<Changeset> changesets = Lists.newArrayList();

        repo.log(null, startRev, endRev, true, true, new ChangesetCollector(changesets));
        return changesets;
      } else {
        log.debug("repository {} is empty, latest revision is 0. Returning empty iterable.", context.getRepository());
        return List.of();
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not open repository: " + repository.getNamespaceAndName(), ex);
    }
  }

  @Override
  public Optional<Changeset> getLatestChangeset() {
    try {
      SVNRepository repo = open();
      long latestRevision = repo.getLatestRevision();
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
