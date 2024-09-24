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

package sonia.scm.api.v2.resources;

import sonia.scm.event.Event;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.Repository;

@Event
public class GitRepositoryConfigChangedEvent {
  private final Repository repository;
  private final GitRepositoryConfig oldConfig;
  private final GitRepositoryConfig newConfig;

  public GitRepositoryConfigChangedEvent(Repository repository, GitRepositoryConfig oldConfig, GitRepositoryConfig newConfig) {
    this.repository = repository;
    this.oldConfig = oldConfig;
    this.newConfig = newConfig;
  }

  public Repository getRepository() {
    return repository;
  }

  public GitRepositoryConfig getOldConfig() {
    return oldConfig;
  }

  public GitRepositoryConfig getNewConfig() {
    return newConfig;
  }
}
