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

import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** Deletes temporary mirror refs, deferring deletion of the current branch until HEAD has moved. */
final class GitMirrorRefDeleter {

  private final Repository repository;
  private final Set<String> deferred = new HashSet<>();

  GitMirrorRefDeleter(Repository repository) {
    this.repository = repository;
  }

  void delete(String name) throws IOException {
    delete(name, true);
  }

  void deleteDeferred() throws IOException {
    for (String name : deferred) {
      delete(name, false);
    }
    deferred.clear();
  }

  private void delete(String name, boolean allowDeferral) throws IOException {
    RefUpdate update = repository.getRefDatabase().newUpdate(name, true);
    update.setForceUpdate(true);
    RefUpdate.Result result = update.delete();
    switch (result) {
      case NEW, NO_CHANGE, FAST_FORWARD, FORCED:
        return;
      case REJECTED_CURRENT_BRANCH:
        if (allowDeferral) {
          deferred.add(name);
          return;
        }
        break;
      default:
        break;
    }
    throw new IOException("Could not delete mirror working copy ref '" + name + "': " + result);
  }
}
