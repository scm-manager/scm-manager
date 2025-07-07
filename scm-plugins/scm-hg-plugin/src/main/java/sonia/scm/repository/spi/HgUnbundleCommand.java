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

import com.google.common.io.ByteSource;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.api.UnbundleResponse;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static sonia.scm.util.Archives.extractTar;

public class HgUnbundleCommand implements UnbundleCommand {
  private static final Logger LOG = LoggerFactory.getLogger(HgUnbundleCommand.class);

  private final HgCommandContext context;
  private final HgLazyChangesetResolver changesetResolver;
  private final HgRepositoryHookEventFactory eventFactory;

  @Inject
  HgUnbundleCommand(@Assisted HgCommandContext context,
                    HgLazyChangesetResolver changesetResolver,
                    HgRepositoryHookEventFactory eventFactory
  ) {
    this.context = context;
    this.changesetResolver = changesetResolver;
    this.eventFactory = eventFactory;
  }

  @Override
  public UnbundleResponse unbundle(UnbundleCommandRequest request) throws IOException {
    ByteSource archive = checkNotNull(request.getArchive(), "archive is required");
    Path repositoryDir = context.getDirectory().toPath();
    LOG.debug("archive repository {} to {}", repositoryDir, archive);

    if (!Files.exists(repositoryDir)) {
      Files.createDirectories(repositoryDir);
    }
    Path hgDir = repositoryDir.resolve(".hg");
    if (Files.exists(hgDir)) {
      IOUtil.delete(hgDir.toFile());
    }

    unbundleRepositoryFromRequest(request, repositoryDir);
    fireHookEvent(request);
    return new UnbundleResponse(0);
  }

  private void fireHookEvent(UnbundleCommandRequest request) {
    RepositoryHookEvent event = eventFactory.createEvent(context, changesetResolver);
    if (event != null) {
      request.getPostEventSink().accept(event);
    }
  }

  private void unbundleRepositoryFromRequest(UnbundleCommandRequest request, Path repositoryDir) throws IOException {
    extractTar(request.getArchive().openBufferedStream(), repositoryDir).run();
  }

  public interface Factory {
    HgUnbundleCommand create(HgCommandContext context);
  }

}
