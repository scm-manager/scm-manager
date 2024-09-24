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

package sonia.scm.importexport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.BadRequestException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;

import java.util.Set;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;

public class RepositoryTypeSupportChecker {

  private RepositoryTypeSupportChecker() {
  }

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryTypeSupportChecker.class);

  /**
   * Check repository type for support for the given command.
   *
   * @param type repository type
   * @param cmd  command
   */
  public static void checkSupport(RepositoryType type, Command cmd) {
    Set<Command> cmds = type.getSupportedCommands();
    if (!cmds.contains(cmd)) {
      LOG.debug("type {} does not support this command {}",
        type.getName(),
        cmd);
      throw new IllegalTypeForImportException("type does not support command");
    }
  }

  @SuppressWarnings("javasecurity:S5145")
  // the type parameter is validated in the resource to only contain valid characters (\w)
  public static RepositoryType type(RepositoryManager manager, String type) {
    RepositoryHandler handler = manager.getHandler(type);
    if (handler == null) {
      LOG.debug("no handler for type {} found", type);
      throw new IllegalTypeForImportException("unsupported repository type: " + type);
    }
    return handler.getType();
  }

  @SuppressWarnings("java:S110") // this is fine for exceptions
  private static class IllegalTypeForImportException extends BadRequestException {
    public IllegalTypeForImportException(String message) {
      super(noContext(), message);
    }

    @Override
    public String getCode() {
      return "CISPvega31";
    }
  }
}
