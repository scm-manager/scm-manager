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

package sonia.scm.repository;


import sonia.scm.Type;
import sonia.scm.repository.api.Command;

import java.util.Collections;
import java.util.Set;

/**
 * The type (mercurial, subversion, git) of a {@link Repository}.
 *
 * @since 1.18
 */
public class RepositoryType extends Type
{
  private Set<Command> supportedCommands;

  private Set<Feature> supportedFeatures;

  /**
   * This constructor is required for JAXB.
   */
  public RepositoryType() {}

  /**
   * Constructs a new {@link RepositoryType} object.
   *
   *
   * @param name name of the type
   * @param displayName display name of the type
   * @param supportedCommands supported commands of the type
   */
  @SuppressWarnings("unchecked")
  public RepositoryType(String name, String displayName,
    Set<Command> supportedCommands)
  {
    this(name, displayName, supportedCommands, Collections.EMPTY_SET);
  }

  /**
   * Constructs a new {@link RepositoryType} object.
   *
   *
   * @param name name of the type
   * @param displayName display name of the type
   * @param supportedCommands supported commands of the type
   * @param supportedFeatures supported features of the type
   *
   * @since 1.25
   */
  public RepositoryType(String name, String displayName,
    Set<Command> supportedCommands, Set<Feature> supportedFeatures)
  {
    super(name, displayName);
    this.supportedCommands = supportedCommands;
    this.supportedFeatures = supportedFeatures;
  }


  /**
   * Returns a set of commands, which are supported by the repository type.
   */
  public Set<Command> getSupportedCommands()
  {
    return supportedCommands;
  }

  /**
   * Returns a set of features, which are supported by the repository type.
   * @since 1.25
   */
  public Set<Feature> getSupportedFeatures()
  {
    return supportedFeatures;
  }

}
