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
 * @since 1.33
 */
public abstract class HookContextProvider
{
  private boolean clientDisconnected = false;

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


  /**
   * Mark client connection as disconnected.
   *
   */
  final void handleClientDisconnect()
  {
    clientDisconnected = true;
  }


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


  /**
   * Creates a new provider specific {@link HookMessageProvider} or throws a {@link HookFeatureIsNotSupportedException}.
   *
   * @return provider specific {@link HookChangesetProvider}
   */
  protected HookMessageProvider createMessageProvider()
  {
    throw new HookFeatureIsNotSupportedException(HookFeature.MESSAGE_PROVIDER);
  }

}
