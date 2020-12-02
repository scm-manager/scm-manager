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

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.ExecutionException;
import com.aragost.javahg.commands.PullCommand;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.WorkingCopy;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class AbstractWorkingCopyCommand extends AbstractCommand {
  static final Pattern HG_MESSAGE_PATTERN = Pattern.compile(".*\\[SCM\\](?: Error:)? (.*)");

  protected final HgWorkingCopyFactory workingCopyFactory;

  public AbstractWorkingCopyCommand(HgCommandContext context, HgWorkingCopyFactory workingCopyFactory)
  {
    super(context);
    this.workingCopyFactory = workingCopyFactory;
  }

  protected List<Changeset> pullChangesIntoCentralRepository(WorkingCopy<Repository, Repository> workingCopy, String branch) {
    try {
      com.aragost.javahg.commands.PullCommand pullCommand = PullCommand.on(workingCopy.getCentralRepository());
      workingCopyFactory.configure(pullCommand);
      return pullCommand.execute(workingCopy.getDirectory().getAbsolutePath());
    } catch (ExecutionException e) {
      throw IntegrateChangesFromWorkdirException
        .withPattern(HG_MESSAGE_PATTERN)
        .forMessage(context.getScmRepository(), e.getMessage());
    } catch (IOException e) {
      throw new InternalRepositoryException(getRepository(),
        String.format("Could not pull changes '%s' into central repository", branch),
        e);
    }
  }
}
