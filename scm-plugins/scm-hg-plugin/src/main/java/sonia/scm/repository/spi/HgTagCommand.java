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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.javahg.Repository;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.TagCreateRequest;
import sonia.scm.repository.api.TagDeleteRequest;
import sonia.scm.repository.work.WorkingCopy;
import sonia.scm.user.User;

import static sonia.scm.repository.spi.UserFormatter.getUserStringFor;

public class HgTagCommand extends AbstractWorkingCopyCommand implements TagCommand {

  public static final String DEFAULT_BRANCH_NAME = "default";

  @Inject
  public HgTagCommand(@Assisted HgCommandContext context, HgRepositoryHandler handler) {
    this(context, handler.getWorkingCopyFactory());
  }

  @VisibleForTesting
  HgTagCommand(HgCommandContext context, HgWorkingCopyFactory workingCopyFactory) {
    super(context, workingCopyFactory);
  }

  @Override
  public Tag create(TagCreateRequest request) {
    try (WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(getContext(), DEFAULT_BRANCH_NAME)) {
      Repository repository = getContext().open();
      String rev = request.getRevision();
      if (Strings.isNullOrEmpty(rev)) {
        rev = repository.tip().getNode();
      }
      org.javahg.commands.TagCommand.on(workingCopy.getWorkingRepository())
        .rev(rev)
        .user(getUserStringFor(SecurityUtils.getSubject().getPrincipals().oneByType(User.class)))
        .execute(request.getName());
      pullChangesIntoCentralRepository(workingCopy, DEFAULT_BRANCH_NAME);
      return new Tag(request.getName(), rev);
    }
  }

  @Override
  public void delete(TagDeleteRequest request) {
    try (WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(getContext(), DEFAULT_BRANCH_NAME)) {
      org.javahg.commands.TagCommand.on(workingCopy.getWorkingRepository())
        .user(getUserStringFor(SecurityUtils.getSubject().getPrincipals().oneByType(User.class)))
        .remove()
        .execute(request.getName());

      pullChangesIntoCentralRepository(workingCopy, DEFAULT_BRANCH_NAME);
    }
  }

  public interface Factory {
    HgTagCommand create(HgCommandContext context);
  }

}
