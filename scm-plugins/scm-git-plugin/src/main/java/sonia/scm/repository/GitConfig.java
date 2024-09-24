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

import com.google.common.base.Strings;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import sonia.scm.auditlog.AuditEntry;


@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
@AuditEntry(labels = {"git", "config"})
public class GitConfig extends RepositoryConfig {

  private static final String FALLBACK_BRANCH = "main";

  @SuppressWarnings("WeakerAccess") // This might be needed for permission checking
  public static final String PERMISSION = "git";

  @XmlElement(name = "gc-expression")
  private String gcExpression;

  @XmlElement(name = "disallow-non-fast-forward")
  private boolean nonFastForwardDisallowed;

  @XmlElement(name = "default-branch")
  private String defaultBranch = FALLBACK_BRANCH;

  @XmlElement(name = "lfs-write-authorization-expiration")
  private int lfsWriteAuthorizationExpirationInMinutes = 5;

  public String getGcExpression() {
    return gcExpression;
  }

  public void setGcExpression(String gcExpression) {
    this.gcExpression = gcExpression;
  }

  public boolean isNonFastForwardDisallowed() {
    return nonFastForwardDisallowed;
  }

  public void setNonFastForwardDisallowed(boolean nonFastForwardDisallowed) {
    this.nonFastForwardDisallowed = nonFastForwardDisallowed;
  }

  public String getDefaultBranch() {
    if (Strings.isNullOrEmpty(defaultBranch)) {
      return FALLBACK_BRANCH;
    }
    return defaultBranch;
  }

  public void setDefaultBranch(String defaultBranch) {
    this.defaultBranch = defaultBranch;
  }

  public int getLfsWriteAuthorizationExpirationInMinutes() {
    return lfsWriteAuthorizationExpirationInMinutes;
  }

  public void setLfsWriteAuthorizationExpirationInMinutes(int lfsWriteAuthorizationExpirationInMinutes) {
    this.lfsWriteAuthorizationExpirationInMinutes = lfsWriteAuthorizationExpirationInMinutes;
  }

  @Override
  @XmlTransient // Only for permission checks, don't serialize to XML
  public String getId() {
    // Don't change this without migrating SCM permission configuration!
    return PERMISSION;
  }
}
