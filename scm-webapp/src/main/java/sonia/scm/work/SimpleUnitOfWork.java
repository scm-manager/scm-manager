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

package sonia.scm.work;

import com.google.inject.Injector;
import lombok.EqualsAndHashCode;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
class SimpleUnitOfWork extends UnitOfWork {

  private final Task task;

  SimpleUnitOfWork(long order, PrincipalCollection principal, Set<Resource> locks, Task task) {
    super(order, principal, locks);
    this.task = task;
  }

  @Override
  protected Task task(Injector injector) {
    injector.injectMembers(task);
    return task;
  }
}
