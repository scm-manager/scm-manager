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

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode
@ToString
public class BrowserResult implements Serializable {

  private String revision;
  private String requestedRevision;
  private FileObject file;
  private boolean modifiable;

  public BrowserResult() {
  }

  public BrowserResult(String revision, FileObject file) {
    this(revision, revision, file);
  }

  public BrowserResult(String revision, FileObject file, boolean modifiable) {
    this(revision, revision, file, modifiable);
  }

  public BrowserResult(String revision, String requestedRevision, FileObject file) {
    this(revision, requestedRevision, file, false);
  }

  public BrowserResult(String revision, String requestedRevision, FileObject file, boolean modifiable) {
    this.revision = revision;
    this.requestedRevision = requestedRevision;
    this.file = file;
    this.modifiable = modifiable;
  }

  public String getRevision() {
    return revision;
  }

  public String getRequestedRevision() {
    return requestedRevision;
  }

  public FileObject getFile() {
    return file;
  }

  /**
   * If this is <code>true</code>, the requested revision represents a modifiable head of the repository or (if
   * supported) a branch and therefore can be modified.
   * @since 3.6.0
   */
  public boolean isModifiable() {
    return modifiable;
  }
}
