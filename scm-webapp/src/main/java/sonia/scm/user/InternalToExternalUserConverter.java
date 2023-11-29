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

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;

@Slf4j
@Extension
public class InternalToExternalUserConverter implements ExternalUserConverter{

  private final ScmConfiguration scmConfiguration;

  @Inject
  public InternalToExternalUserConverter(ScmConfiguration scmConfiguration) {
    this.scmConfiguration = scmConfiguration;
  }

  public User convert(User user) {
    if (shouldConvertUser(user)) {
      log.info("Convert internal user {} to external", user.getId());
      user.setExternal(true);
      user.setPassword(null);
    }
    return user;
  }

  private boolean shouldConvertUser(User user) {
    return !user.isExternal() && scmConfiguration.isEnabledUserConverter();
  }
}
