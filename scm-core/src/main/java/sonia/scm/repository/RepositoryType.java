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
