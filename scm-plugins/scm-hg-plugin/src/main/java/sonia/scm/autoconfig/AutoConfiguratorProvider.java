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

package sonia.scm.autoconfig;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.Platform;
import sonia.scm.repository.HgVerifier;
import sonia.scm.util.SystemUtil;

public class AutoConfiguratorProvider implements Provider<AutoConfigurator> {

  private final HgVerifier verifier;
  private final Platform platform;

  @Inject
  public AutoConfiguratorProvider(HgVerifier verifier) {
    this(verifier, SystemUtil.getPlatform());
  }

  @VisibleForTesting
  AutoConfiguratorProvider(HgVerifier verifier, Platform platform) {
    this.verifier = verifier;
    this.platform = platform;
  }

  @Override
  public AutoConfigurator get() {
    if (platform.isPosix()) {
      return new PosixAutoConfigurator(verifier, System.getenv());
    } else if (platform.isWindows()) {
      return new WindowsAutoConfigurator(verifier, new WindowsRegistry(), System.getenv());
    } else {
      return new NoOpAutoConfigurator();
    }
  }
}
