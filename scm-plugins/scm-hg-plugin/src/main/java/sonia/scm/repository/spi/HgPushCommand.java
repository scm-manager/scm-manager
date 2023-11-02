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

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import org.javahg.Changeset;
import org.javahg.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.api.PushFailedException;
import sonia.scm.repository.api.PushResponse;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
public class HgPushCommand extends AbstractHgPushOrPullCommand implements PushCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgPushCommand.class);

  private final TemporaryConfigFactory configFactory;

  @Inject
  public HgPushCommand(HgRepositoryHandler handler, @Assisted HgCommandContext context, TemporaryConfigFactory configFactory) {
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
      result = builder.call(() -> {
        org.javahg.commands.PushCommand hgPush = org.javahg.commands.PushCommand.on(open());

        if (request.isForce()) {
          hgPush.force();
        }

        return hgPush.execute(url);
      });
    } catch (ExecutionException ex) {
      throw new PushFailedException(getRepository());
    }

    return new PushResponse(result.size());
  }

  public interface Factory {
    HgPushCommand create(HgCommandContext context);
  }

}
