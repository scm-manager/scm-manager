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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookContextProvider;
import sonia.scm.repository.spi.HookMergeDetectionProvider;

/**
 * The context for all repository hooks. With the {@link HookContext} class it
 * is able to send messages back to the client, retrieve {@link Changeset}s
 * which are added during this push/commit and gives information about changed
 * branches and tags.
 *
 * @since 1.33
 */
public final class HookContext {

 
  private static final Logger logger =
    LoggerFactory.getLogger(HookContext.class);

  private final PreProcessorUtil preProcessorUtil;

  private final HookContextProvider provider;

  private final Repository repository;


  HookContext(HookContextProvider provider, Repository repository, PreProcessorUtil preProcessorUtil) {
    this.provider = provider;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
  }


  /**
   * Returns a {@link HookBranchProvider} which is able to return information
   * about changed branches during the current hook.
   *
   * @return {@link HookBranchProvider}
   *
   * @throws HookFeatureIsNotSupportedException if the feature is not supported
   *  by the underlying provider
   *
   * @since 1.45
   */
  public HookBranchProvider getBranchProvider() {
    logger.debug("create branch provider for repository {}", repository);

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
  public HookTagProvider getTagProvider() {
    logger.debug("create tag provider for repository {}", repository);

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
  public HookChangesetBuilder getChangesetProvider() {
    logger.debug("create changeset provider for repository {}", repository);

    return new HookChangesetBuilder(
      repository,
      preProcessorUtil,
      provider.getChangesetProvider()
    );
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
  public HookModificationsProvider getModificationsProvider() {
    logger.debug("create diff provider for repository {}", repository);

    return provider.getModificationsProvider();
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
  public HookMessageProvider getMessageProvider() {
    logger.debug("create message provider for repository {}", repository);

    return provider.getMessageProvider();
  }

  /**
   * Returns a {@link HookMergeDetectionProvider} which is able to check whether two
   * branches have been merged with the incoming changesets.
   *
   * @return {@link HookMergeDetectionProvider} which is able to detect merges.
   *
   * @throws HookFeatureIsNotSupportedException if the feature is not supported
   *  by the underlying provider
   */
  public HookMergeDetectionProvider getMergeDetectionProvider() {
    logger.debug("create merge detection provider for repository {}", repository);

    return provider.getMergeDetectionProvider();
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

}
