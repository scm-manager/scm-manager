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


import sonia.scm.plugin.ExtensionPoint;

/**
 * The BlameLinePreProcessorFactory create {@link BlameLinePreProcessor}
 * objects for a specific repository.
 *
 * @since 1.17
 */
@ExtensionPoint
public interface BlameLinePreProcessorFactory
        extends PreProcessorFactory<BlameLine>
{

  /**
   * Create a new {@link BlameLinePreProcessor} for the given repository.
   *
   *
   * @param repository repository
   *
   * @return {@link BlameLinePreProcessor} for the given repository
   */
  @Override
  public BlameLinePreProcessor createPreProcessor(Repository repository);
}
