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

package sonia.scm.migration;

import sonia.scm.version.Version;

/**
 * Base class for update steps, telling the target version and the affected data type.
 *
 * @since 2.14.0
 */
public interface UpdateStepTarget {
  /**
   * Declares the new version of the data type given by {@link #getAffectedDataType()}. A update step will only be
   * executed, when this version is bigger than the last recorded version for its data type according to
   * {@link Version#compareTo(Version)}
   */
  Version getTargetVersion();

  /**
   * Declares the data type this update step will take care of. This should be a qualified name, like
   * <code>com.example.myPlugin.configuration</code>.
   */
  String getAffectedDataType();
}
