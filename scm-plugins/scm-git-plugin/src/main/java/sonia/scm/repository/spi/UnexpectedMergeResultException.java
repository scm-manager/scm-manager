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

import org.eclipse.jgit.api.MergeResult;
import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.Repository;

class UnexpectedMergeResultException extends ExceptionWithContext {

  public static final String CODE = "4GRrgkSC01";

  public UnexpectedMergeResultException(Repository repository, MergeResult result) {
    super(ContextEntry.ContextBuilder.entity(repository).build(), createMessage(result));
  }

  private static String createMessage(MergeResult result) {
    return "unexpected merge result: " + result
      + "\nconflicts: " + result.getConflicts()
      + "\ncheckout conflicts: " + result.getCheckoutConflicts()
      + "\nfailing paths: " + result.getFailingPaths();
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
