/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookContextProvider;

/**
 * The context for all repository hooks. With the {@link HookContext} class it
 * is able to send messages back to the client, retrieve {@link Changeset}s
 * which are added during this push/commit and gives informations about changed 
 * branches and tags.
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public final class HookContext
{

  /**
   * the logger for HookContext
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HookContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param provider
   * @param repository
   * @param preProcessorUtil
   */
  HookContext(HookContextProvider provider, Repository repository,
    PreProcessorUtil preProcessorUtil)
  {
    this.provider = provider;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a {@link HookBranchProvider} which is able to return informations
   * about changed branches during the current hook.
   *
   * @return {@link HookBranchProvider}
   * 
   * @throws HookFeatureIsNotSupportedException if the feature is not supported 
   *  by the underlying provider
   * 
   * @since 1.45
   */
  public HookBranchProvider getBranchProvider()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create branch provider for repository {}",
        repository.getName());
    }

    return provider.getBranchProvider();
  }
  
 /**
   * Returns a {@link HookTagProvider} which is able to return informations
   * about changed tags during the current hook.
   *
   * @return {@link HookTagProvider}
   * 
   * @throws HookFeatureIsNotSupportedException if the feature is not supported 
   *  by the underlying provider
   * 
   * @since 1.50
   */
  public HookTagProvider getTagProvider()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create tag provider for repository {}",
        repository.getName());
    }

    return provider.getTagProvider();
  }

  /**
   * Returns a {@link HookChangesetBuilder} which is able to return all
   * {@link Changeset}'s during this push/commit.
   *
   *
   * @return {@link HookChangesetBuilder}
   * 
   * @throws HookFeatureIsNotSupportedException if the feature is not supported 
   *  by the underlying provider
   */
  public HookChangesetBuilder getChangesetProvider()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create changeset provider for repository {}",
        repository.getName());
    }

    //J-
    return new HookChangesetBuilder(
      repository, 
      preProcessorUtil,
      provider.getChangesetProvider()
    );
    //J+
  }

  /**
   * Returns a {@link HookMessageProvider} which is able to send message back to
   * the scm client.
   * <strong>Note:</strong> The {@link HookMessageProvider} is only available if
   * the underlying {@link HookContextProvider} supports the handling of
   * messages and the hook is executed synchronous.
   *
   * @return {@link HookMessageProvider} which is able to send message back to
   * the scm client
   * 
   * @throws HookFeatureIsNotSupportedException if the feature is not supported 
   *  by the underlying provider
   */
  public HookMessageProvider getMessageProvider()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create message provider for repository {}",
        repository.getName());
    }

    return provider.getMessageProvider();
  }

  /**
   * Returns true if the underlying provider support the requested feature.
   *
   *
   * @param feature feature to check if it is supported
   *
   * @return true if the feature is supported
   */
  public boolean isFeatureSupported(HookFeature feature)
  {
    return provider.getSupportedFeatures().contains(feature);
  }

  //~--- fields ---------------------------------------------------------------

  /** pre processor util */
  private final PreProcessorUtil preProcessorUtil;

  /** hook context provider */
  private final HookContextProvider provider;

  /** repository */
  private final Repository repository;
}
