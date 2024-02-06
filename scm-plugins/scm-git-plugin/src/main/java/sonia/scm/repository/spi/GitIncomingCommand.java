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
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.ObjectId;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryHandler;

import java.io.IOException;


public class GitIncomingCommand extends AbstractGitIncomingOutgoingCommand
  implements IncomingCommand {

  @Inject
  GitIncomingCommand(@Assisted GitContext context, GitRepositoryHandler handler, GitChangesetConverterFactory converterFactory) {
    super(context, handler, converterFactory);
  }


  @Override
  public ChangesetPagingResult getIncomingChangesets(IncomingCommandRequest request) throws IOException {
    return getIncomingOrOutgoingChangesets(request);
  }

  @Override
  protected void prepareLogCommand(LogCommand logCommand, ObjectId localId,
    ObjectId remoteId)
    throws IOException
  {
    if (localId != null)
    {
      logCommand.not(localId);
    }

    logCommand.add(remoteId);
  }


  @Override
  protected boolean retrieveChangesets(ObjectId localId, ObjectId remoteId)
  {
    return remoteId != null;
  }

  public interface Factory {
    IncomingCommand create(GitContext context);
  }

}
