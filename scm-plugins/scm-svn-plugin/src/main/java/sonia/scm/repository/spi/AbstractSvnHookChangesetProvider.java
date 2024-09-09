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

import com.google.common.collect.ImmutableSet;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.HookChangesetProvider;

import java.util.Collections;


public abstract class AbstractSvnHookChangesetProvider
  implements HookChangesetProvider
{
  private HookChangesetResponse response;

  public abstract RepositoryHookType getType();



  protected abstract Changeset fetchChangeset();


  @Override
  @SuppressWarnings("unchecked")
  public synchronized HookChangesetResponse handleRequest(
    HookChangesetRequest request)
  {
    if (response == null)
    {
      Changeset c = fetchChangeset();
      Iterable<Changeset> iterable;

      if (c == null)
      {
        iterable = Collections.EMPTY_SET;
      }
      else
      {
        iterable = ImmutableSet.of(c);
      }

      response = new HookChangesetResponse(iterable);
    }

    return response;
  }

}
