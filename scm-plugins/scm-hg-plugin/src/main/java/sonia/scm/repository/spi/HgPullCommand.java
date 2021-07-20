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

import com.aragost.javahg.Changeset;
import com.aragost.javahg.commands.ExecutionException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class HgPullCommand extends AbstractHgPushOrPullCommand implements PullCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgPullCommand.class);
  private final ScmEventBus eventBus;
  private final HgLazyChangesetResolver changesetResolver;
  private final HgRepositoryHookEventFactory eventFactory;

  public HgPullCommand(HgRepositoryHandler handler,
                       HgCommandContext context,
                       ScmEventBus eventBus,
                       HgLazyChangesetResolver changesetResolver,
                       HgRepositoryHookEventFactory eventFactory
  ) {
    super(handler, context);
    this.eventBus = eventBus;
    this.changesetResolver = changesetResolver;
    this.eventFactory = eventFactory;
  }

  @Override
  @SuppressWarnings({"java:S3252"})
  public PullResponse pull(PullCommandRequest request)
    throws IOException {
    String url = getRemoteUrl(request);
    HgIniConfigurator iniConfigurator = new HgIniConfigurator(getContext());

    LOG.debug("pull changes from {} to {}", url, getContext().getScmRepository());

    List<Changeset> result;

    if (!Strings.isNullOrEmpty(request.getUsername()) && !Strings.isNullOrEmpty(request.getPassword())) {
      iniConfigurator.addAuthenticationConfig(request, url);
    }

    try {
      result = com.aragost.javahg.commands.PullCommand.on(open()).execute(url);
    } catch (ExecutionException ex) {
      throw new ImportFailedException(ContextEntry.ContextBuilder.entity(getRepository()).build(), "could not execute pull command", ex);
    } finally {
      iniConfigurator.removeAuthenticationConfig();
    }

    firePostReceiveRepositoryHookEvent();

    return new PullResponse(result.size());
  }

  private void firePostReceiveRepositoryHookEvent() {
    eventBus.post(eventFactory.createEvent(context, changesetResolver));
  }
}
