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
import org.javahg.commands.ExecutionException;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.web.HgUtil;

import javax.inject.Inject;
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
