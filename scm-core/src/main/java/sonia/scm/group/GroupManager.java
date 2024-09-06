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


import sonia.scm.Manager;
import sonia.scm.search.Searchable;

import java.util.Collection;
import java.util.Set;

/**
 * The central class for managing {@link Group}s.
 * This class is a singleton and is available via injection.
 *
 */
public interface GroupManager
        extends Manager<Group>, Searchable<Group>
{

  /**
   * Returns a {@link Collection} of all groups assigned to the given member.
   */
  Collection<Group> getGroupsForMember(String member);

  /**
   * Returns a {@link Set} of all group names.
   *
   * @since 2.42.0
   */
  Set<String> getAllNames();
}
