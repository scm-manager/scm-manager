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

package sonia.scm.config;


import ch.qos.logback.core.PropertyDefinerBase;
import com.google.common.annotations.VisibleForTesting;
import sonia.scm.ConfigurationException;
import sonia.scm.Platform;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.util.SystemUtil;

import java.io.File;
import java.util.Properties;

/**
 * Resolve directory path for SCM-Manager logs.
 *
 */
public class ScmLogFilePropertyDefiner extends PropertyDefinerBase {

  private final String logDirectoryPath;

  public ScmLogFilePropertyDefiner() {
    this(SCMContext.getContext(), SystemUtil.getPlatform(), System.getProperties());
  }

  @VisibleForTesting
  ScmLogFilePropertyDefiner(SCMContextProvider context, Platform platform, Properties properties) {
    File logDirectory = resolveDirectory(context, platform, properties);

    if (!logDirectory.exists() && !logDirectory.mkdirs()) {
      throw new ConfigurationException(
        "could not create log directory ".concat(logDirectory.getPath()));
    }

    this.logDirectoryPath = logDirectory.getAbsolutePath();
  }

  private File resolveDirectory(SCMContextProvider context, Platform platform, Properties properties) {
    if (platform.isMac()) {
      return resolveOsX(properties);
    } else {
      return resolveDefault(context);
    }
  }

  private File resolveOsX(Properties properties) {
    return new File(properties.getProperty("user.home"), "Library/Logs/SCM-Manager");
  }

  private File resolveDefault(SCMContextProvider context) {
    return new File(context.getBaseDirectory(), "logs");
  }

  @Override
  public String getPropertyValue() {
    return logDirectoryPath;
  }

}
