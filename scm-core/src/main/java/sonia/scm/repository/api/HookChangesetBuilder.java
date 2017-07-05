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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.io.DeepCopy;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetRequest;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

//~--- JDK imports ------------------------------------------------------------

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
    Iterable<Changeset> changesets =
      provider.handleRequest(request).getChangesets();

    if (!disablePreProcessors)
    {
      final Function<Changeset, Changeset> changesetFunction = c -> {
        Changeset copy = null;

        try {
          copy = DeepCopy.copy(c);
          preProcessorUtil.prepareForReturn(repository, copy,
                                            !disableEscaping);
        } catch (IOException ex) {
          logger.error("could not create a copy of changeset", ex);
        }

        if (copy == null) {
          copy = c;
        }

        return copy;
      };
      changesets = Streams.stream(changesets)
                          .map(changesetFunction::apply)
                          .collect(Collectors.toList());
    }

    return changesets;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Disable html escaping for the returned changesets. By default all
   * changesets are html escaped.
   *
   *
   * @param disableEscaping true to disable the html escaping
   *
   * @return {@code this}
   *
   * @since 1.35
   */
  public HookChangesetBuilder setDisableEscaping(boolean disableEscaping)
  {
    this.disableEscaping = disableEscaping;

    return this;
  }

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

  /** disable escaping */
  private boolean disableEscaping = false;

  /** disable pre processors marker */
  private boolean disablePreProcessors = false;

  /** PreProcessorUtil */
  private PreProcessorUtil preProcessorUtil;

  /** provider implementation */
  private HookChangesetProvider provider;

  /** changed repository */
  private Repository repository;
}
