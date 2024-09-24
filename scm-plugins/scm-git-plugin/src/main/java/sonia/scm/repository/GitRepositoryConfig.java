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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.auditlog.AuditEntry;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
@AuditEntry(labels = {"git", "config"})
public class GitRepositoryConfig {

  public GitRepositoryConfig() {
  }

  public GitRepositoryConfig(String defaultBranch) {
    this.defaultBranch = defaultBranch;
  }

  private String defaultBranch;
  private boolean nonFastForwardDisallowed;

  public String getDefaultBranch() {
    return defaultBranch;
  }

  public void setDefaultBranch(String defaultBranch) {
    this.defaultBranch = defaultBranch;
  }

  public boolean isNonFastForwardDisallowed() { return nonFastForwardDisallowed; }

  public void setNonFastForwardDisallowed(boolean nonFastForwardDisallowed) {
    this.nonFastForwardDisallowed = nonFastForwardDisallowed;
  }
}
