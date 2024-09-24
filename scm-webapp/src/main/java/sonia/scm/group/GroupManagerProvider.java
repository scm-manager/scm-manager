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


import com.google.inject.Inject;
import com.google.inject.Provider;

import sonia.scm.Undecorated;
import sonia.scm.util.Decorators;

import java.util.Set;


public class GroupManagerProvider implements Provider<GroupManager>
{
  @Inject(optional = true)
  private Set<GroupManagerDecoratorFactory> decoratorFactories;

  @Inject
  @Undecorated
  private Provider<GroupManager> groupManagerProvider;
  
  @Override
  public GroupManager get()
  {
    return Decorators.decorate(groupManagerProvider.get(), decoratorFactories);
  }



  public void setDecoratorFactories(
    Set<GroupManagerDecoratorFactory> decoratorFactories)
  {
    this.decoratorFactories = decoratorFactories;
  }


  public void setGroupManagerProvider(
    Provider<GroupManager> groupManagerProvider)
  {
    this.groupManagerProvider = groupManagerProvider;
  }

}
