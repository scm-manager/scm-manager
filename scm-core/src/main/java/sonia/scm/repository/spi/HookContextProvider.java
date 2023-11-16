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

package sonia.scm.repository.spi;

import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookException;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookFeatureIsNotSupportedException;
import sonia.scm.repository.api.HookMessageProvider;
import sonia.scm.repository.api.HookModificationsProvider;
import sonia.scm.repository.api.HookTagProvider;

import java.util.Set;

/**
 * Repository type specific provider for {@link HookContext}.
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public abstract class HookContextProvider
{

  /**
   * Return the provider specific {@link HookMessageProvider} or throws a {@link HookFeatureIsNotSupportedException}.
   * The method will throw a {@link HookException} if the client is already disconnected.
   *
   * @return provider specific {@link HookMessageProvider}
   */
  public final HookMessageProvider getMessageProvider()
  {
    if (clientDisconnected)
    {
      throw new HookException(
        "message provider is only available in a synchronous hook execution.");
    }

    return createMessageProvider();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Mark client connection as disconnected.
   *
   */
  final void handleClientDisconnect()
  {
    clientDisconnected = true;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a set of supported hook features of the client.
   *
   * @return supported features
   */
  public abstract Set<HookFeature> getSupportedFeatures();

  /**
   * Return the provider specific {@link HookBranchProvider} or throws a {@link HookFeatureIsNotSupportedException}.
   *
   * @return provider specific {@link HookBranchProvider}
   *
   * @since 1.45
   */
  public HookBranchProvider getBranchProvider()
  {
    throw new HookFeatureIsNotSupportedException(HookFeature.BRANCH_PROVIDER);
  }

  /**
   * Return the provider specific {@link HookTagProvider} or throws a {@link HookFeatureIsNotSupportedException}.
   *
   * @return provider specific {@link HookTagProvider}
   *
   * @since 1.50
   */
  public HookTagProvider getTagProvider()
  {
    throw new HookFeatureIsNotSupportedException(HookFeature.TAG_PROVIDER);
  }

  /**
   * Return the provider specific {@link HookChangesetProvider} or throws a {@link HookFeatureIsNotSupportedException}.
   *
   * @return provider specific {@link HookChangesetProvider}
   */
  public HookChangesetProvider getChangesetProvider()
  {
    throw new HookFeatureIsNotSupportedException(HookFeature.CHANGESET_PROVIDER);
  }

  /**
   * Return the provider specific {@link HookMergeDetectionProvider} or throws a {@link HookFeatureIsNotSupportedException}.
   *
   * @return provider specific {@link HookMergeDetectionProvider}
   */
  public HookMergeDetectionProvider getMergeDetectionProvider()
  {
    throw new HookFeatureIsNotSupportedException(HookFeature.MERGE_DETECTION_PROVIDER);
  }

  public HookModificationsProvider getModificationsProvider() {
    throw new HookFeatureIsNotSupportedException(HookFeature.MODIFICATIONS_PROVIDER);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new provider specific {@link HookMessageProvider} or throws a {@link HookFeatureIsNotSupportedException}.
   *
   * @return provider specific {@link HookChangesetProvider}
   */
  protected HookMessageProvider createMessageProvider()
  {
    throw new HookFeatureIsNotSupportedException(HookFeature.MESSAGE_PROVIDER);
  }

  //~--- fields ---------------------------------------------------------------

  private boolean clientDisconnected = false;
}
