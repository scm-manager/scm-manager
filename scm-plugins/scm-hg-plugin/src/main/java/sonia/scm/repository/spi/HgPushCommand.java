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
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.api.PushFailedException;
import sonia.scm.repository.api.PushResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


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
      result = builder.call((Path configFile) -> {
        org.javahg.commands.PushCommand hgPush = org.javahg.commands.PushCommand.on(open());

        if (request.isForce()) {
          hgPush.force();
        }

        if(configFile != null) {
          hgPush.cmdAppend("--config-file", configFile.toFile().getAbsolutePath());
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
