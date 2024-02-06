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
