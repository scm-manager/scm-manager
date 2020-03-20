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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.io.DeepCopy;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 * The {@link HookChangesetBuilder} is able to return all {@link Changeset}s
 * which are added during the current push/commit.
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public final class HookChangesetBuilder
{

  /** request */
  private static final HookChangesetRequest request =
    new HookChangesetRequest();

  /**
   * the logger for HookChangesetBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HookChangesetBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new HookChangesetBuilder.
   *
   *
   * @param repository changed repository
   * @param preProcessorUtil pre processor util
   * @param provider implementation of {@link HookChangesetProvider}
   */
  HookChangesetBuilder(Repository repository,
    PreProcessorUtil preProcessorUtil, HookChangesetProvider provider)
  {
    this.repository = repository;
    this.provider = provider;
    this.preProcessorUtil = preProcessorUtil;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a immutable {@link List} of added {@link Changeset}'s.
   * <strong>Note:</strong> Use this method only if you need a {@link List}, if
   * you just want to iterate over the {@link Changeset}'s use
   * {@link #getChangesets()} instead. The {@link #getChangesets()} needs less
   * memory and should be much more faster then this method.
   *
   * @return immutable {@link List} of added {@link Changeset}'s
   */
  public List<Changeset> getChangesetList()
  {
    return ImmutableList.copyOf(getChangesets());
  }

  /**
   * Returns an {@link Iterable} which is able to return all {@link Changeset}s
   * which are added to the repository.
   *
   * @return {@link Iterable} for added {@link Changeset}s
   */
  public Iterable<Changeset> getChangesets()
  {
    HookChangesetResponse hookChangesetResponse = provider.handleRequest(request);
    Iterable<Changeset> changesets = hookChangesetResponse.getChangesets();

    if (!disablePreProcessors)
    {
      changesets = Iterables.transform(changesets,
        new Function<Changeset, Changeset>()
      {

        @Override
        public Changeset apply(Changeset c)
        {
          Changeset copy = null;

          try
          {
            copy = DeepCopy.copy(c);
            preProcessorUtil.prepareForReturn(repository, copy);
          }
          catch (IOException ex)
          {
            logger.error("could not create a copy of changeset", ex);
          }

          if (copy == null)
          {
            copy = c;
          }

          return copy;
        }

      });
    }

    return changesets;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Disable the execution of pre processors.
   *
   *
   * @param disablePreProcessors true to disable the pre processors execution
   *
   * @return {@code this}
   */
  public HookChangesetBuilder setDisablePreProcessors(
    boolean disablePreProcessors)
  {
    this.disablePreProcessors = disablePreProcessors;

    return this;
  }

  //~--- fields ---------------------------------------------------------------

  /** disable pre processors marker */
  private boolean disablePreProcessors = false;

  /** PreProcessorUtil */
  private PreProcessorUtil preProcessorUtil;

  /** provider implementation */
  private HookChangesetProvider provider;

  /** changed repository */
  private Repository repository;
}
