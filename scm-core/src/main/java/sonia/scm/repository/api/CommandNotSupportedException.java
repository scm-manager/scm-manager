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
    
package sonia.scm.repository.api;

/**
 * This exception is thrown if the {@link RepositoryService} does not support
 * the requested command.
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class CommandNotSupportedException extends RuntimeException
{

  /**
   * Constructs a new {@link CommandNotSupportedException}.
   *
   *
   * @param command not supported command
   */
  public CommandNotSupportedException(Command command)
  {
    //J-
    this(
      command,
      "The requested feature "
      .concat(command.toString())
      .concat(" is not supported")
    );
    //J+
  }

  /**
   * Constructs a new {@link CommandNotSupportedException}.
   *
   *
   * @param command not supported command
   * @param message message to be shown
   */
  public CommandNotSupportedException(Command command, String message)
  {
    super(message);
    this.command = command;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the command which is not supported.
   *
   *
   * @return not supported command
   */
  public Command getCommand()
  {
    return command;
  }

  //~--- fields ---------------------------------------------------------------

  /** not supported command */
  private Command command;
}
