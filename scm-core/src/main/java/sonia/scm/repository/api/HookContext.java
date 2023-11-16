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

//~--- non-JDK imports --------------------------------------------------------

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
 * which are added during this push/commit and gives informations about changed
 * branches and tags.
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public final class HookContext {

  /**
   * the logger for HookContext
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HookContext.class);

  /**
   * Constructs ...
   *
   *
   * @param provider
   * @param repository
   * @param preProcessorUtil
   */
  HookContext(HookContextProvider provider, Repository repository, PreProcessorUtil preProcessorUtil) {
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

  //~--- fields ---------------------------------------------------------------

  /** pre processor util */
  private final PreProcessorUtil preProcessorUtil;

  /** hook context provider */
  private final HookContextProvider provider;

  /** repository */
  private final Repository repository;
}
