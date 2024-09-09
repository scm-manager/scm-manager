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
