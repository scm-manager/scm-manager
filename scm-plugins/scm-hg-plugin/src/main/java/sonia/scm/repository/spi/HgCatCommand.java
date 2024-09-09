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
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.web.HgUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HgCatCommand extends AbstractCommand implements CatCommand {

  private static final Logger log = LoggerFactory.getLogger(HgCatCommand.class);

  @Inject
  HgCatCommand(@Assisted HgCommandContext context) {
    super(context);
  }

  @Override
  public void getCatResult(CatCommandRequest request, OutputStream output) throws IOException {
    InputStream input = getCatResultStream(request);
    try {
      ByteStreams.copy(input, output);
    } finally {
      Closeables.close(input, true);
    }
  }

  @Override
  public InputStream getCatResultStream(CatCommandRequest request) throws IOException {
    org.javahg.commands.CatCommand cmd =
      org.javahg.commands.CatCommand.on(open());

    cmd.rev(HgUtil.getRevision(request.getRevision()));

    try {
      return cmd.execute(request.getPath());
    } catch (ExecutionException e) {
      log.error("could not execute cat command", e);
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity(getRepository()), "could not execute cat command", e);
    }
  }

  public interface Factory {
    HgCatCommand create(HgCommandContext context);
  }

}
