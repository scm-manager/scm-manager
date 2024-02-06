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
