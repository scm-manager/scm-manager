/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.ManagerDaoAdapter;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContextProvider;
import sonia.scm.security.RepositoryPermissionProvider;
import sonia.scm.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Singleton @EagerSingleton
public class DefaultRepositoryRoleManager extends AbstractRepositoryRoleManager
{

  /** the logger for XmlRepositoryRoleManager */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultRepositoryRoleManager.class);

  @Inject
  public DefaultRepositoryRoleManager(RepositoryRoleDAO repositoryRoleDAO, RepositoryPermissionProvider repositoryPermissionProvider)
  {
    this.repositoryRoleDAO = repositoryRoleDAO;
    this.managerDaoAdapter = new ManagerDaoAdapter<>(repositoryRoleDAO);
    this.repositoryPermissionProvider = repositoryPermissionProvider;
  }

  @Override
  public void close() {
    // do nothing
  }

  @Override
  public RepositoryRole create(RepositoryRole repositoryRole) {
    String type = repositoryRole.getType();
    if (Util.isEmpty(type)) {
      repositoryRole.setType(repositoryRoleDAO.getType());
    }

    logger.info("create repositoryRole {} of type {}", repositoryRole.getName(), repositoryRole.getType());

    return managerDaoAdapter.create(
      repositoryRole,
      RepositoryRolePermissions::modify,
      newRepositoryRole -> fireEvent(HandlerEventType.BEFORE_CREATE, newRepositoryRole),
      newRepositoryRole -> fireEvent(HandlerEventType.CREATE, newRepositoryRole)
    );
  }

  @Override
  public void delete(RepositoryRole repositoryRole) {
    logger.info("delete repositoryRole {} of type {}", repositoryRole.getName(), repositoryRole.getType());
    managerDaoAdapter.delete(
      repositoryRole,
      RepositoryRolePermissions::modify,
      toDelete -> fireEvent(HandlerEventType.BEFORE_DELETE, toDelete),
      toDelete -> fireEvent(HandlerEventType.DELETE, toDelete)
    );
  }

  @Override
  public void init(SCMContextProvider context) {
  }

  @Override
  public void modify(RepositoryRole repositoryRole) {
    logger.info("modify repositoryRole {} of type {}", repositoryRole.getName(), repositoryRole.getType());
    managerDaoAdapter.modify(
      repositoryRole,
      x -> RepositoryRolePermissions.modify(),
      notModified -> fireEvent(HandlerEventType.BEFORE_MODIFY, repositoryRole, notModified),
      notModified -> fireEvent(HandlerEventType.MODIFY, repositoryRole, notModified));
  }

  @Override
  public void refresh(RepositoryRole repositoryRole) {
    logger.info("refresh repositoryRole {} of type {}", repositoryRole.getName(), repositoryRole.getType());

    RepositoryRolePermissions.read().check();
    RepositoryRole fresh = repositoryRoleDAO.get(repositoryRole.getName());

    if (fresh == null) {
      throw new NotFoundException(RepositoryRole.class, repositoryRole.getName());
    }
  }

  @Override
  public RepositoryRole get(String id) {
    RepositoryRolePermissions.read();

    return findSystemRole(id).orElse(findCustomRole(id));
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
    return repositoryPermissionProvider.availableRoles().stream().filter(role -> role.getName().equals(id)).findFirst();
  }

  @Override
  public Collection<RepositoryRole> getAll() {
    return getAll(repositoryRole -> true, null);
  }

  @Override
  public Collection<RepositoryRole> getAll(Predicate<RepositoryRole> filter, Comparator<RepositoryRole> comparator) {
    List<RepositoryRole> repositoryRoles = new ArrayList<>();

    if (!RepositoryRolePermissions.read().isPermitted()) {
      return Collections.emptySet();
    }
    for (RepositoryRole repositoryRole : repositoryPermissionProvider.availableRoles()) {
      repositoryRoles.add(repositoryRole.clone());
    }

    if (comparator != null) {
      Collections.sort(repositoryRoles, comparator);
    }

    return repositoryRoles;
  }

  @Override
  public Collection<RepositoryRole> getAll(Comparator<RepositoryRole> comaparator, int start, int limit) {
    if (!RepositoryRolePermissions.read().isPermitted()) {
      return Collections.emptySet();
    }
    return Util.createSubCollection(repositoryRoleDAO.getAll(), comaparator,
      (collection, item) -> {
        collection.add(item.clone());
      }, start, limit);
  }

  @Override
  public Collection<RepositoryRole> getAll(int start, int limit)
  {
    return getAll(null, start, limit);
  }

  @Override
  public Long getLastModified()
  {
    return repositoryRoleDAO.getLastModified();
  }

  private final RepositoryRoleDAO repositoryRoleDAO;
  private final ManagerDaoAdapter<RepositoryRole> managerDaoAdapter;
  private final RepositoryPermissionProvider repositoryPermissionProvider;
}
