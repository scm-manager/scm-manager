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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sonia.scm.Validateable;

import sonia.scm.repository.Person;
import sonia.scm.repository.util.AuthorUtil.CommandWithAuthor;

import java.util.Optional;

/**
 * This class contains the information to run {@link RevertCommand#revert(RevertCommandRequest)}.

 * @since 3.8
 */
@Setter
@ToString
public class RevertCommandRequest implements Validateable, CommandWithAuthor {

  @Getter
  private Person author;

  @Getter
  private String revision;

  /**
   * Reverts can be signed with a GPG key. This is set as <tt>true</tt> by default.
   */
  @Getter
  private boolean sign = true;

  private String branch;

  private String message;

  public Optional<String> getBranch() {
    return Optional.ofNullable(branch);
  }

  public Optional<String> getMessage() {
    return Optional.ofNullable(message);
  }

  @Override
  public boolean isValid() {
    boolean validBranch = branch == null || !branch.isEmpty();
    boolean validMessage = message == null || !message.isEmpty();
    return revision != null && author != null && validBranch && validMessage;
  }

}
