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
        cmd.name());
      throw new IllegalTypeForImportException("type does not support command");
    }
  }

  @SuppressWarnings("javasecurity:S5145") // the type parameter is validated in the resource to only contain valid characters (\w)
  public static RepositoryType type(RepositoryManager manager, String type) {
    RepositoryHandler handler = manager.getHandler(type);
    if (handler == null) {
      LOG.debug("no handler for type {} found", type);
      throw new IllegalTypeForImportException("unsupported repository type: " + type);
    }
    return handler.getType();
  }

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
