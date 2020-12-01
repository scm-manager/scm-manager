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

import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.PullCommand;
import com.google.common.base.Strings;
import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.TagCreateRequest;
import sonia.scm.repository.api.TagDeleteRequest;
import sonia.scm.repository.work.WorkingCopy;

import java.io.IOException;

public class HgTagCommand extends AbstractCommand implements TagCommand {

  public static final String DEFAULT_BRANCH_NAME = "default";
  private final HgWorkingCopyFactory workingCopyFactory;

  public HgTagCommand(HgCommandContext context, HgWorkingCopyFactory workingCopyFactory) {
    super(context);
    this.workingCopyFactory = workingCopyFactory;
  }

  @Override
  public Tag create(TagCreateRequest request) {
    try (WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(getContext(), DEFAULT_BRANCH_NAME)) {
      Repository repository = getContext().open();
      String rev = request.getRevision();
      if (Strings.isNullOrEmpty(rev)) {
        rev = repository.tip().getNode();
      }
      com.aragost.javahg.commands.TagCommand.on(workingCopy.getWorkingRepository())
        .rev(rev)
        .user(SecurityUtils.getSubject().getPrincipal().toString())
        .execute(request.getName());
      pullChangesIntoCentralRepository(workingCopy, DEFAULT_BRANCH_NAME);
      return new Tag(request.getName(), rev);
    }
  }

  @Override
  public void delete(TagDeleteRequest request) {
    try (WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(getContext(), DEFAULT_BRANCH_NAME)) {
      com.aragost.javahg.commands.TagCommand.on(workingCopy.getWorkingRepository())
        .user(SecurityUtils.getSubject().getPrincipal().toString())
        .remove()
        .execute(request.getName());

      pullChangesIntoCentralRepository(workingCopy, DEFAULT_BRANCH_NAME);
    }
  }

  private void pullChangesIntoCentralRepository(WorkingCopy<com.aragost.javahg.Repository, com.aragost.javahg.Repository> workingCopy, String branch) {
    try {
      com.aragost.javahg.commands.PullCommand pullCommand = PullCommand.on(workingCopy.getCentralRepository());
      workingCopyFactory.configure(pullCommand);
      pullCommand.execute(workingCopy.getDirectory().getAbsolutePath());
    } catch (IOException e) {
      throw new InternalRepositoryException(getRepository(),
        String.format("Could not pull changes '%s' into central repository", branch),
        e);
    }
  }
}
