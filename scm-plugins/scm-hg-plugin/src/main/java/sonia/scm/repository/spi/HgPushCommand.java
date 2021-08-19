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

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Changeset;
import com.aragost.javahg.commands.ExecutionException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PushResponse;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class HgPushCommand extends AbstractHgPushOrPullCommand implements PushCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgPushCommand.class);

  private final TemporaryConfigFactory configFactory;

  @Inject
  public HgPushCommand(HgRepositoryHandler handler, HgCommandContext context, TemporaryConfigFactory configFactory) {
    super(handler, context);
    this.configFactory = configFactory;
  }

  @Override
  @SuppressWarnings("java:S3252") // this is how javahg is used
  public PushResponse push(PushCommandRequest request) throws IOException {
    String url = getRemoteUrl(request);

    LOG.debug("push changes from {} to {}", getRepository(), url);

    TemporaryConfigFactory.Builder builder = configFactory.withContext(context);
    if (!Strings.isNullOrEmpty(request.getUsername()) && !Strings.isNullOrEmpty(request.getPassword())) {
      builder.withCredentials(url, request.getUsername(), request.getPassword());
    }

    List<Changeset> result;

    try {
      result = com.aragost.javahg.commands.PushCommand.on(open()).execute(url);
    } catch (ExecutionException ex) {
      throw new ImportFailedException(entity(getRepository()).build(), "could not execute pull command", ex);
    }

    return new PushResponse(result.size());
  }
}
