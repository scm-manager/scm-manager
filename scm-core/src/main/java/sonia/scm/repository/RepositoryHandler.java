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


import sonia.scm.Handler;
import sonia.scm.FeatureNotSupportedException;
import sonia.scm.plugin.ExtensionPoint;

/**
 * Handler class for a specific {@link Repository} type.
 * These classes are singletons.
 *
 */
@ExtensionPoint
public interface RepositoryHandler
        extends Handler<Repository>
{


  /**
   * Returns the {@link ImportHandler} for the repository type of this handler.
   *
   *
   * @return {@link ImportHandler} for the repository type of this handler
   * @since 1.12
   * @deprecated
   *
   * @throws FeatureNotSupportedException
   */
  @Deprecated
  public ImportHandler getImportHandler() throws FeatureNotSupportedException;

  /**
   * Returns information about the version of the RepositoryHandler.
   * @since 1.15
   */
  public String getVersionInformation();

  @Override
  RepositoryType getType();
}
