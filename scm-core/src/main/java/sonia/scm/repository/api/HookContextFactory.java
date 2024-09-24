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

package sonia.scm.repository.api;


import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookContextProvider;

/**
 * Injectable factory for {@link HookContext} objects.
 *
 * @since 1.33
 */
public final class HookContextFactory
{

  private static final Logger logger =
    LoggerFactory.getLogger(HookContextFactory.class);

  private PreProcessorUtil preProcessorUtil;

  @Inject
  public HookContextFactory(PreProcessorUtil preProcessorUtil)
  {
    this.preProcessorUtil = preProcessorUtil;
  }


  /**
   * Creates a new {@link HookContext}.
   *
   *
   * @param provider provider implementation
   * @param repository changed repository
   *
   * @return new {@link HookContext}
   */
  public HookContext createContext(HookContextProvider provider,
    Repository repository)
  {
    logger.debug("create new hook context for repository {}",
      repository.getName());

    return new HookContext(provider, repository, preProcessorUtil);
  }

}
