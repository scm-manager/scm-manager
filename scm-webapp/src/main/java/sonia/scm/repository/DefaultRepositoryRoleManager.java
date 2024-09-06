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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.ManagerDaoAdapter;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContextProvider;
import sonia.scm.auditlog.Auditor;
import sonia.scm.security.RepositoryPermissionProvider;
import sonia.scm.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
@EagerSingleton
public class DefaultRepositoryRoleManager extends AbstractRepositoryRoleManager {

 
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultRepositoryRoleManager.class);

  private final RepositoryRoleDAO repositoryRoleDAO;
  private final ManagerDaoAdapter<RepositoryRole> managerDaoAdapter;
  private final RepositoryPermissionProvider repositoryPermissionProvider;

  @Inject
  public DefaultRepositoryRoleManager(RepositoryRoleDAO repositoryRoleDAO,
                                      RepositoryPermissionProvider repositoryPermissionProvider,
                                      Set<Auditor> auditors) {
    this.repositoryRoleDAO = repositoryRoleDAO;
    this.managerDaoAdapter = new ManagerDaoAdapter<>(repositoryRoleDAO, auditors);
    this.repositoryPermissionProvider = repositoryPermissionProvider;
  }

  @Override
  public void close() {
    // do nothing
  }

  @Override
  public RepositoryRole create(RepositoryRole repositoryRole) {
    assertNoSystemRole(repositoryRole);
    String type = repositoryRole.getType();
    if (Util.isEmpty(type)) {
      repositoryRole.setType(repositoryRoleDAO.getType());
    }

    logger.info("create repositoryRole {} of type {}", repositoryRole.getName(), repositoryRole.getType());

    return managerDaoAdapter.create(
      repositoryRole,
      RepositoryRolePermissions::write,
      newRepositoryRole -> fireEvent(HandlerEventType.BEFORE_CREATE, newRepositoryRole),
      newRepositoryRole -> fireEvent(HandlerEventType.CREATE, newRepositoryRole)
    );
  }

  @Override
  public void delete(RepositoryRole repositoryRole) {
    assertNoSystemRole(repositoryRole);
    logger.info("delete repositoryRole {} of type {}", repositoryRole.getName(), repositoryRole.getType());
    managerDaoAdapter.delete(
      repositoryRole,
      RepositoryRolePermissions::write,
      toDelete -> fireEvent(HandlerEventType.BEFORE_DELETE, toDelete),
      toDelete -> fireEvent(HandlerEventType.DELETE, toDelete)
    );
  }

  @Override
  public void init(SCMContextProvider context) {
    // Nothing
  }

  @Override
  public void modify(RepositoryRole repositoryRole) {
    assertNoSystemRole(repositoryRole);
    logger.info("modify repositoryRole {} of type {}", repositoryRole.getName(), repositoryRole.getType());
    managerDaoAdapter.modify(
      repositoryRole,
      x -> RepositoryRolePermissions.write(),
      notModified -> fireEvent(HandlerEventType.BEFORE_MODIFY, repositoryRole, notModified),
      notModified -> fireEvent(HandlerEventType.MODIFY, repositoryRole, notModified));
  }

  @Override
  public void refresh(RepositoryRole repositoryRole) {
    logger.info("refresh repositoryRole {} of type {}", repositoryRole.getName(), repositoryRole.getType());

    RepositoryRole fresh = repositoryRoleDAO.get(repositoryRole.getName());

    if (fresh == null) {
      throw new NotFoundException(RepositoryRole.class, repositoryRole.getName());
    }
  }

  @Override
  public RepositoryRole get(String id) {
    return findSystemRole(id).orElse(findCustomRole(id));
  }

  private void assertNoSystemRole(RepositoryRole repositoryRole) {
    if (findSystemRole(repositoryRole.getId()).isPresent()) {
      throw new UnauthorizedException("system roles cannot be modified");
    }
  }

  private RepositoryRole findCustomRole(String id) {
    RepositoryRole repositoryRole = repositoryRoleDAO.get(id);

    if (repositoryRole != null) {
      return repositoryRole.clone();
    } else {
      return null;
    }
  }

  private Optional<RepositoryRole> findSystemRole(String id) {
    return repositoryPermissionProvider
      .availableRoles()
      .stream()
      .filter(role -> !repositoryRoleDAO.getType().equals(role.getType()))
      .filter(role -> role.getName().equals(id)).findFirst();
  }

  @Override
  public List<RepositoryRole> getAll() {
    List<RepositoryRole> repositoryRoles = new ArrayList<>();

    for (RepositoryRole repositoryRole : repositoryPermissionProvider.availableRoles()) {
      repositoryRoles.add(repositoryRole.clone());
    }

    return repositoryRoles;
  }

  @Override
  public Collection<RepositoryRole> getAll(Predicate<RepositoryRole> filter, Comparator<RepositoryRole> comparator) {
    List<RepositoryRole> repositoryRoles = getAll();

    List<RepositoryRole> filteredRoles = repositoryRoles.stream().filter(filter::test).collect(Collectors.toList());

    if (comparator != null) {
      filteredRoles.sort(comparator);
    }

    return filteredRoles;
  }

  @Override
  public Collection<RepositoryRole> getAll(Comparator<RepositoryRole> comaparator, int start, int limit) {
    return Util.createSubCollection(getAll(), comaparator,
      (collection, item) -> collection.add(item.clone()), start, limit);
  }

  @Override
  public Collection<RepositoryRole> getAll(int start, int limit) {
    return getAll(null, start, limit);
  }

  @Override
  public Long getLastModified() {
    return repositoryRoleDAO.getLastModified();
  }

}
