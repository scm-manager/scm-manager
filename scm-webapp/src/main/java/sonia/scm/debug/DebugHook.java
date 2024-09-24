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

package sonia.scm.debug;

import com.github.legman.ReferenceType;
import com.github.legman.Subscribe;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;

/**
 * {@link PostReceiveRepositoryHookEvent} which stores receives data and passes it to the {@link DebugService}.
 *
 */
@EagerSingleton
public final class DebugHook
{
 
  private static final Logger LOG = LoggerFactory.getLogger(DebugHook.class);

  private final DebugService debugService;

  @Inject
  public DebugHook(DebugService debugService)
  {
    this.debugService = debugService;
  }

  /**
   * Processes the received {@link PostReceiveRepositoryHookEvent} and transforms it to a {@link DebugHookData} and
   * passes it to the {@link DebugService}.
   *
   * @param event received event
   */
  @Subscribe(referenceType = ReferenceType.STRONG)
  public void processEvent(PostReceiveRepositoryHookEvent event){
    LOG.trace("store changeset ids from repository {}", event.getRepository());

    debugService.put(
      event.getRepository().getNamespaceAndName(),
      new DebugHookData(Collections2.transform(
        event.getContext().getChangesetProvider().getChangesetList(), IDEXTRACTOR)
      ));
  }

  private static final Function<Changeset, String> IDEXTRACTOR = (Changeset changeset) -> changeset.getId();
}
