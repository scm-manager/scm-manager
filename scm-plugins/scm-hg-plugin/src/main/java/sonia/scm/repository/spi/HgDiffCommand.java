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
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.javahg.Repository;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;
import sonia.scm.repository.spi.javahg.HgDiffInternalCommand;
import sonia.scm.web.HgUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class HgDiffCommand extends AbstractCommand implements DiffCommand {

  @Inject
  HgDiffCommand(@Assisted HgCommandContext context) {
    super(context);
  }

  @Override
  public DiffCommandBuilder.OutputStreamConsumer getDiffResult(DiffCommandRequest request) {
    return output -> {
      Repository hgRepo = open();
      try {
        diff(hgRepo, request, output);
      } finally {
        getContext().close();
      }
    };
  }

  @Override
  public DiffCommandBuilder.OutputStreamConsumer getDiffResultInternal(DiffCommandRequest request) {
    return output -> {
      Repository hgRepo = open();
      diff(hgRepo, request, output);
    };
  }

  private void diff(Repository hgRepo, DiffCommandRequest request, OutputStream output) throws IOException {
    HgDiffInternalCommand cmd = createDiffCommand(hgRepo, request);
    try (InputStream inputStream = streamDiff(hgRepo, cmd, request.getPath())) {
      ByteStreams.copy(inputStream, output);
    }
  }

  @Nonnull
  private HgDiffInternalCommand createDiffCommand(Repository hgRepo, DiffCommandRequest request) {
    HgDiffInternalCommand cmd = HgDiffInternalCommand.on(hgRepo);
    DiffFormat format = request.getFormat();
    if (format == DiffFormat.GIT) {
      cmd.git();
    }
    String revision = HgUtil.getRevision(request.getRevision());
    if (request.getIgnoreWhitespaceLevel() == IgnoreWhitespaceLevel.ALL) {
      cmd.cmdAppend("-w");
    }
    if (request.getAncestorChangeset() != null) {
      String ancestor = HgUtil.getRevision(request.getAncestorChangeset());
      cmd.cmdAppend(String.format("-r ancestor(%s,%s):%s", ancestor, revision, revision));
    } else {
      cmd.change(revision);
    }
    return cmd;
  }

  private InputStream streamDiff(Repository hgRepo, HgDiffInternalCommand cmd, String path) {
    if (!Strings.isNullOrEmpty(path)) {
      return cmd.stream(hgRepo.file(path));
    } else {
      return cmd.stream();
    }
  }

  public interface Factory {
    HgDiffCommand create(HgCommandContext context);
  }

}
