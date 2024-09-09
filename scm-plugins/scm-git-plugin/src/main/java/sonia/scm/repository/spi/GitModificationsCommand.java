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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modifications;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;


@Slf4j
public class GitModificationsCommand extends AbstractGitCommand implements ModificationsCommand {

  @Inject
  public GitModificationsCommand(@Assisted GitContext context) {
    super(context);
  }

  @Override
  @SuppressWarnings("java:S2093")
  public Modifications getModifications(String baseRevision, String revision) {
    try {
      return new ModificationsComputer(open()).compute(baseRevision, revision);
    } catch (IOException ex) {
      log.error("could not open repository: " + repository.getNamespaceAndName(), ex);
      throw new InternalRepositoryException(entity(repository), "could not open repository: " + repository.getNamespaceAndName(), ex);
    }
  }


  @Override
  public Modifications getModifications(String revision) {
    return getModifications(null, revision);
  }

  public interface Factory {
    ModificationsCommand create(GitContext context);
  }

}
