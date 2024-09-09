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

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.javahg.Changeset;
import org.javahg.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullResponse;

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
