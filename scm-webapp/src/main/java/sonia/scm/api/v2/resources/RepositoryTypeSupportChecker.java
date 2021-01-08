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

package sonia.scm.api.v2.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Type;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Set;

class RepositoryTypeSupportChecker {

  private RepositoryTypeSupportChecker() {
  }

  private static final Logger logger = LoggerFactory.getLogger(RepositoryTypeSupportChecker.class);

  /**
   * Check repository type for support for the given command.
   *
   * @param type repository type
   * @param cmd  command
   */
  static void checkSupport(Type type, Command cmd) {
    if (!(type instanceof RepositoryType)) {
      logger.warn("type {} is not a repository type", type.getName());
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Set<Command> cmds = ((RepositoryType) type).getSupportedCommands();
    if (!cmds.contains(cmd)) {
      logger.warn("type {} does not support this command {}",
        type.getName(),
        cmd.name());
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  static Type type(RepositoryManager manager, String type) {
    RepositoryHandler handler = manager.getHandler(type);
    if (handler == null) {
      logger.warn("no handler for type {} found", type);
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return handler.getType();
  }
}
