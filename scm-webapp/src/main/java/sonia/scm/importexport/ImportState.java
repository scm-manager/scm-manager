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

  private Repository repository;

  private boolean environmentChecked;
  private boolean storeImported;
  private boolean repositoryImported;

  private Collection<RepositoryPermission> repositoryPermissions;

  private Path temporaryRepositoryBundle;

  private final List<Object> pendingEvents = new ArrayList<>();

  ImportState(Repository repository) {
    this.repository = repository;
  }

  public void setRepository(Repository repository) {
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

  public Collection<Object> getPendingEvents() {
    return Collections.unmodifiableCollection(pendingEvents);
  }
}
