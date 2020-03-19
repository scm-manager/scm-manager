/**
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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.api.HookFeature;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnHookContextProvider extends HookContextProvider
{

  /** Field description */
  private static final Set<HookFeature> SUPPORTED_FEATURES =
    EnumSet.of(HookFeature.CHANGESET_PROVIDER);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param changesetProvider
   */
  public SvnHookContextProvider(
    AbstractSvnHookChangesetProvider changesetProvider)
  {
    this.changesetProvider = changesetProvider;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public AbstractSvnHookChangesetProvider getChangesetProvider()
  {
    return changesetProvider;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private AbstractSvnHookChangesetProvider changesetProvider;
}
