/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

import com.aragost.javahg.commands.ExecutionException;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.web.HgUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HgCatCommand extends AbstractCommand implements CatCommand {

  private static final Logger log = LoggerFactory.getLogger(HgCatCommand.class);

  HgCatCommand(HgCommandContext context, Repository repository) {
    super(context, repository);
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
    com.aragost.javahg.commands.CatCommand cmd =
      com.aragost.javahg.commands.CatCommand.on(open());

    cmd.rev(HgUtil.getRevision(request.getRevision()));

    try {
      return cmd.execute(request.getPath());
    } catch (ExecutionException e) {
      log.error("could not execute cat command", e);
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity(getRepository()), "could not execute cat command", e);
    }
  }
}
