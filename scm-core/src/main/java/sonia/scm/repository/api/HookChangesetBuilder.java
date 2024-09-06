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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.io.DeepCopy;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;

import java.io.IOException;
import java.util.List;

/**
 * The {@link HookChangesetBuilder} is able to return all {@link Changeset}s
 * which are added during the current push/commit.
 *
 * @since 1.33
 */
public final class HookChangesetBuilder
{
  private boolean disablePreProcessors = false;

  private PreProcessorUtil preProcessorUtil;

  private HookChangesetProvider provider;

  /** changed repository */
  private Repository repository;

  private static final HookChangesetRequest request =
    new HookChangesetRequest();


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


  /**
   * Returns an immutable {@link List} of added {@link Changeset}'s.
   * <strong>Note:</strong> Use this method only if you need a {@link List}, if
   * you just want to iterate over the {@link Changeset}'s use
   * {@link #getChangesets()} instead. The {@link #getChangesets()} needs less
   * memory and should be much faster than this method.
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
    return applyPreprocessorsIfNotDisabled(changesets);
  }

  public Iterable<Changeset> getRemovedChangesets() {
    HookChangesetResponse hookChangesetResponse = provider.handleRequest(request);
    Iterable<Changeset> changesets = hookChangesetResponse.getRemovedChangesets();
    return applyPreprocessorsIfNotDisabled(changesets);
  }

  private Iterable<Changeset> applyPreprocessorsIfNotDisabled(Iterable<Changeset> changesets) {
    if (disablePreProcessors) {
      return changesets;
    }

    return Iterables.transform(changesets, c -> {
      Changeset copy = null;

      try {
        copy = DeepCopy.copy(c);
        preProcessorUtil.prepareForReturn(repository, copy);
      } catch (IOException ex) {
        logger.error("could not create a copy of changeset", ex);
      }

      if (copy == null) {
        copy = c;
      }

      return copy;
    });
  }


  /**
   * Disable the execution of pre processors if set to <code>true</code>.
   *
   * @return {@code this}
   */
  public HookChangesetBuilder setDisablePreProcessors(
    boolean disablePreProcessors)
  {
    this.disablePreProcessors = disablePreProcessors;

    return this;
  }

}
