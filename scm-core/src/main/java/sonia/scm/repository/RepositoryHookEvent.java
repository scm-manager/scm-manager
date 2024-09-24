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


import sonia.scm.repository.api.HookContext;

/**
 * Repository hook event represents an change event of a repository.
 *
 * @since 1.6
 */
public class RepositoryHookEvent
{

  /** context of current hook */
  private final HookContext context;

  /** modified repository */
  private final Repository repository;

  /** hook type */
  private final RepositoryHookType type;

  public RepositoryHookEvent(HookContext context, Repository repository,
    RepositoryHookType type)
  {
    this.context = context;
    this.repository = repository;
    this.type = type;
  }


  public HookContext getContext()
  {
    return context;
  }

  
  public Repository getRepository()
  {
    return repository;
  }

  
  public RepositoryHookType getType()
  {
    return type;
  }

  @Override
  public String toString() {
    return "RepositoryHookEvent{" +
      "repository=" + repository +
      ", type=" + type +
      '}';
  }
}
