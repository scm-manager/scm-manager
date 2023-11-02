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

import com.google.inject.assistedinject.Assisted;
import org.javahg.Changeset;
import org.javahg.commands.ExecutionException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullResponse;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class HgPullCommand extends AbstractHgPushOrPullCommand implements PullCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgPullCommand.class);
  private final ScmEventBus eventBus;
  private final HgLazyChangesetResolver changesetResolver;
  private final HgRepositoryHookEventFactory eventFactory;
  private final TemporaryConfigFactory configFactory;

  @Inject
  public HgPullCommand(HgRepositoryHandler handler,
                       @Assisted HgCommandContext context,
                       ScmEventBus eventBus,
                       @Assisted HgLazyChangesetResolver changesetResolver,
                       @Assisted HgRepositoryHookEventFactory eventFactory,
                       TemporaryConfigFactory configFactory
  ) {
    super(handler, context);
    this.eventBus = eventBus;
    this.changesetResolver = changesetResolver;
    this.eventFactory = eventFactory;
    this.configFactory = configFactory;
  }

  @Override
  @SuppressWarnings({"java:S3252"})
  public PullResponse pull(PullCommandRequest request) throws IOException {
    String url = getRemoteUrl(request);

    LOG.debug("pull changes from {} to {}", url, getContext().getScmRepository());

    TemporaryConfigFactory.Builder builder = configFactory.withContext(context);
    if (!Strings.isNullOrEmpty(request.getUsername()) && !Strings.isNullOrEmpty(request.getPassword())) {
      builder.withCredentials(url, request.getUsername(), request.getPassword());
    }

    List<Changeset> result;

    try {
      result = builder.call(() -> org.javahg.commands.PullCommand.on(open()).execute(url));
    } catch (ExecutionException ex) {
      throw new ImportFailedException(entity(getRepository()).build(), "could not execute pull command", ex);
    }

    firePostReceiveRepositoryHookEvent();

    return new PullResponse(result.size());
  }

  private void firePostReceiveRepositoryHookEvent() {
    eventBus.post(eventFactory.createEvent(context, changesetResolver));
  }

  public interface Factory {
    HgPullCommand create(HgCommandContext context, HgLazyChangesetResolver hgLazyChangesetResolver, HgRepositoryHookEventFactory hgRepositoryHookEventFactory);
  }
}
