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
