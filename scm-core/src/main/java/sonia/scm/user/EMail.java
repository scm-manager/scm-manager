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

package sonia.scm.user;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.ValidationUtil;

/**
 * Email is able to resolve email addresses of users.
 *
 * @since 2.8.0
 */
public class EMail {

  private final ScmConfiguration scmConfiguration;

  @Inject
  public EMail(ScmConfiguration scmConfiguration) {
    this.scmConfiguration = scmConfiguration;
  }

  /**
   * Returns the email address of the given user or a generated fallback address.
   * @param user user to resolve address from
   * @return email address or fallback
   */
  public String getMailOrFallback(User user) {
    return getMailOrFallback(DisplayUser.from(user));
  }

  /**
   * Returns the email address of the given user or a generated fallback address.
   * @param user user to resolve address from
   * @return email address or fallback
   */
  public String getMailOrFallback(DisplayUser user) {
    if (Strings.isNullOrEmpty(user.getMail())) {
      if (isMailUsedAsId(user)) {
        return user.getId();
      } else {
        return createFallbackMail(user);
      }
    } else {
      return user.getMail();
    }
  }

  private boolean isMailUsedAsId(DisplayUser user) {
    return ValidationUtil.isMailAddressValid(user.getId());
  }

  private String createFallbackMail(DisplayUser user) {
    return user.getId() + "@" + scmConfiguration.getMailDomainName();
  }
}
