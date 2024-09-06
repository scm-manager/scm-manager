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

package sonia.scm.group;


import sonia.scm.ManagerDecorator;
import sonia.scm.search.SearchRequest;

import java.util.Collection;
import java.util.Set;

/**
 * Decorator for {@link GroupManager}.
 *
 * @since 1.23
 */
public class GroupManagerDecorator
  extends ManagerDecorator<Group> implements GroupManager
{

  public GroupManagerDecorator(GroupManager decorated)
  {
    super(decorated);
    this.decorated = decorated;
  }


  @Override
  public Collection<Group> search(SearchRequest searchRequest)
  {
    return decorated.search(searchRequest);
  }


  /**
   * Returns the decorated {@link GroupManager}.
   * @since 1.34
   */
  public GroupManager getDecorated()
  {
    return decorated;
  }

  @Override
  public Collection<Group> getGroupsForMember(String member)
  {
    return decorated.getGroupsForMember(member);
  }

  @Override
  public Set<String> getAllNames() {
    return decorated.getAllNames();
  }

  private final GroupManager decorated;
}
