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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookException;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookFeatureIsNotSupportedException;
import sonia.scm.repository.api.HookMessageProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookTagProvider;

//~--- JDK imports ------------------------------------------------------------

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
