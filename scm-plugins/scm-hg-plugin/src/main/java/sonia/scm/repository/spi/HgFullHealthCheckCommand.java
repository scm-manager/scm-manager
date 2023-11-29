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
import jakarta.inject.Inject;
import org.javahg.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.HealthCheckResult;

import java.io.IOException;

public class HgFullHealthCheckCommand extends AbstractCommand implements FullHealthCheckCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgFullHealthCheckCommand.class);

  @Inject
  public HgFullHealthCheckCommand(@Assisted HgCommandContext context) {
    super(context);
  }

  @Override
  public HealthCheckResult check() throws IOException {
    HgVerifyCommand cmd = HgVerifyCommand.on(open());
    try {
      cmd.execute();
      return HealthCheckResult.healthy();
    } catch (ExecutionException e) {
      LOG.warn("hg verify failed for repository {}", getRepository(), e);
      return HealthCheckResult.unhealthy(new HealthCheckFailure("FaSUYbZUR1",
        "hg verify failed", "The check 'hg verify' failed for the repository."));
    }
  }

  public interface Factory {
    HgFullHealthCheckCommand create(HgCommandContext context);
  }

}
