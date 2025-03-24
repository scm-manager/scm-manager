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

package sonia.scm.repository.api;

import com.google.common.base.Preconditions;
import jakarta.annotation.Nullable;
import sonia.scm.repository.spi.RevertCommand;
import sonia.scm.repository.spi.RevertCommandRequest;
import sonia.scm.repository.util.AuthorUtil;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.EMail;

/**
 * Applies a revert of a chosen changeset onto the given repository/branch combination.
 *
 * @since 3.8
 */
public final class RevertCommandBuilder {

  private final RevertCommand command;
  private final RevertCommandRequest request;

  @Nullable
  private final EMail email;

  /**
   * @param command A {@link RevertCommand} implementation provided by some source.
   */
  public RevertCommandBuilder(RevertCommand command, @Nullable EMail email) {
    this.command = command;
    this.email = email;
    this.request = new RevertCommandRequest();
  }

  /**
   * Use this to set the author of the revert commit manually. If this is omitted, the currently logged-in user will be
   * used instead. If the given user object does not have an email address, we will use {@link EMail} to compute a
   * fallback address.
   *
   * @param author Author entity.
   * @return This instance.
   */
  public RevertCommandBuilder setAuthor(DisplayUser author) {
    request.setAuthor(AuthorUtil.createAuthorWithMailFallback(author, email));
    return this;
  }

  /**
   * Obligatory value.
   *
   * @param revision Identifier of the revision.
   * @return This instance.
   */
  public RevertCommandBuilder setRevision(String revision) {
    request.setRevision(revision);
    return this;
  }

  /**
   * This is an optional parameter. Not every SCM system supports branches.
   * If null or empty and supported by the SCM, the default branch of the repository shall be used.
   *
   * @param branch Name of the branch.
   * @return This instance.
   */
  public RevertCommandBuilder setBranch(String branch) {
    request.setBranch(branch);
    return this;
  }

  /**
   * This is an optional parameter. If null or empty, a default message will be set.
   *
   * @param message Particular message.
   * @return This instance.
   */
  public RevertCommandBuilder setMessage(String message) {
    request.setMessage(message);
    return this;
  }

  /**
   * Executes the revert with the given builder parameters.
   *
   * @return {@link RevertCommandResult} with information about the executed revert.
   */
  public RevertCommandResult execute() {
    AuthorUtil.setAuthorIfNotAvailable(request, email);
    Preconditions.checkArgument(request.isValid(), "Revert request is invalid, request was: %s", request);
    return command.revert(request);
  }

  protected RevertCommandRequest getRequest() {
    return this.request;
  }
}
