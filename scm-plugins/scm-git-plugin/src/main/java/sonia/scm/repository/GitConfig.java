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

package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import com.google.common.base.Strings;
import sonia.scm.auditlog.AuditEntry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Sebastian Sdorra
 */
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
