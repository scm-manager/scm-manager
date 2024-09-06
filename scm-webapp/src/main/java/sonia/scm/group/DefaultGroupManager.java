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

import com.github.sdorra.ssp.PermissionActionCheck;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.ManagerDaoAdapter;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContextProvider;
import sonia.scm.auditlog.Auditor;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;


@Singleton
public class DefaultGroupManager extends AbstractGroupManager {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultGroupManager.class);
  private final GroupDAO groupDAO;
  private final ManagerDaoAdapter<Group> managerDaoAdapter;

  @Inject
  public DefaultGroupManager(GroupDAO groupDAO, Set<Auditor> auditors) {
    this.groupDAO = groupDAO;
    this.managerDaoAdapter = new ManagerDaoAdapter<>(groupDAO, auditors);
  }

  @Override
  public void close() throws IOException {
    // do nothing
  }

  @Override
  public Group create(Group group) {
    String type = group.getType();
    if (Util.isEmpty(type)) {
      group.setType(groupDAO.getType());
    }

    LOG.info("create group {} of type {}", group.getName(), group.getType());

    removeDuplicateMembers(group);

    return managerDaoAdapter.create(
      group,
      GroupPermissions::create,
      newGroup -> fireEvent(HandlerEventType.BEFORE_CREATE, newGroup),
      newGroup -> fireEvent(HandlerEventType.CREATE, newGroup)
    );
  }

  @Override
  public void delete(Group group) {
    LOG.info("delete group {} of type {}", group.getName(), group.getType());
    managerDaoAdapter.delete(
      group,
      () -> GroupPermissions.delete(group.getName()),
      toDelete -> fireEvent(HandlerEventType.BEFORE_DELETE, toDelete),
      toDelete -> fireEvent(HandlerEventType.DELETE, toDelete)
    );
  }

  @Override
  public void init(SCMContextProvider context) {
  }

  @Override
  public void modify(Group group) {
    LOG.info("modify group {} of type {}", group.getName(), group.getType());

    managerDaoAdapter.modify(
      group,
      GroupPermissions::modify,
      notModified -> {
        removeDuplicateMembers(group);
        fireEvent(HandlerEventType.BEFORE_MODIFY, group, notModified);
      },
      notModified -> fireEvent(HandlerEventType.MODIFY, group, notModified)
    );
  }

  @Override
  public void refresh(Group group) {
    String name = group.getName();
    LOG.info("refresh group {} of type {}", name, group.getType());

    GroupPermissions.read(name).check();
    Group fresh = groupDAO.get(name);

    if (fresh == null) {
      throw new NotFoundException(Group.class, group.getId());
    }

    fresh.copyProperties(group);
  }

  @Override
  public Collection<Group> search(final SearchRequest searchRequest) {
    LOG.debug("search group with query {}", searchRequest.getQuery());

    final PermissionActionCheck<Group> check = GroupPermissions.read();
    return SearchUtil.search(searchRequest, groupDAO.getAll(),
      group -> {
        if (check.isPermitted(group) && matches(searchRequest, group)) {
          return group.clone();
        }
        return null;
      });
  }

  private boolean matches(SearchRequest searchRequest, Group group) {
    return SearchUtil.matchesOne(searchRequest, group.getName(), group.getDescription());
  }

  @Override
  public Group get(String id) {
    GroupPermissions.read(id).check();

    Group group = groupDAO.get(id);

    if (group != null) {
      group = group.clone();
    }

    return group;
  }

  @Override
  public Collection<Group> getAll() {
    return getAll(group -> true, null);
  }

  @Override
  public Collection<Group> getAll(Predicate<Group> filter, Comparator<Group> comparator) {
    List<Group> groups = new ArrayList<>();

    PermissionActionCheck<Group> check = GroupPermissions.read();
    for (Group group : groupDAO.getAll()) {
      if (filter.test(group) && check.isPermitted(group)) {
        groups.add(group.clone());
      }
    }

    if (comparator != null) {
      Collections.sort(groups, comparator);
    }

    return groups;
  }

  @Override
  public Collection<Group> getAll(Comparator<Group> comparator, int start,
                                  int limit) {
    final PermissionActionCheck<Group> check = GroupPermissions.read();

    return Util.createSubCollection(groupDAO.getAll(), comparator,
      (collection, group) -> {
        if (check.isPermitted(group)) {
          collection.add(group.clone());
        }
      }, start, limit);
  }

  @Override
  public Collection<Group> getAll(int start, int limit) {
    return getAll(null, start, limit);
  }

  @Override
  public Collection<Group> getGroupsForMember(String member) {
    LinkedList<Group> groups = new LinkedList<>();

    for (Group group : groupDAO.getAll()) {
      if (group.isMember(member)) {
        groups.add(group.clone());
      }
    }

    return groups;
  }

  @Override
  public Long getLastModified() {
    return groupDAO.getLastModified();
  }

  @Override
  public Set<String> getAllNames() {
    GroupPermissions.list().check();
    return groupDAO.getAll().stream().map(Group::getName).collect(toSet());
  }

  /**
   * Remove duplicate members from group.
   * Have a look at issue #439
   *
   * @param group group
   */
  private void removeDuplicateMembers(Group group) {
    List<String> members =
      Lists.newArrayList(ImmutableSet.copyOf(group.getMembers()));

    group.setMembers(members);
  }
}
