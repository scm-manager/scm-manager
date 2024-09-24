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

package sonia.scm.importexport;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class ImportState {

  private final RepositoryImportLogger logger;

  private Repository repository;

  private boolean environmentChecked;
  private boolean storeImported;
  private boolean repositoryImported;

  private Collection<RepositoryPermission> repositoryPermissions;

  private Path temporaryRepositoryBundle;

  private final List<Object> pendingEvents = new ArrayList<>();

  ImportState(Repository repository, RepositoryImportLogger logger) {
    this.logger = logger;
    this.repository = repository;
  }

  public Repository getRepository() {
    return repository;
  }

  public void environmentChecked() {
    environmentChecked = true;
  }

  public boolean isEnvironmentChecked() {
    return environmentChecked;
  }

  void setPermissions(Collection<RepositoryPermission> repositoryPermissions) {
    this.repositoryPermissions = repositoryPermissions;
  }

  Collection<RepositoryPermission> getRepositoryPermissions() {
    return Collections.unmodifiableCollection(repositoryPermissions);
  }

  public boolean success() {
    return environmentChecked && repositoryImported;
  }

  public void storeImported() {
    this.storeImported = true;
  }

  public boolean isStoreImported() {
    return storeImported;
  }

  public void setTemporaryRepositoryBundle(Path path) {
    this.temporaryRepositoryBundle = path;
  }

  public Optional<Path> getTemporaryRepositoryBundle() {
    return Optional.ofNullable(temporaryRepositoryBundle);
  }

  public void repositoryImported() {
    this.repositoryImported = true;
  }

  public void addPendingEvent(Object event) {
    this.pendingEvents.add(event);
  }

  RepositoryImportLogger getLogger() {
    return logger;
  }

  public Collection<Object> getPendingEvents() {
    return Collections.unmodifiableCollection(pendingEvents);
  }
}
