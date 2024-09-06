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

import com.github.legman.Subscribe;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;
import sonia.scm.web.security.AdministrationContext;

import java.util.Collection;
import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Singleton
public class DefaultNamespaceManager implements NamespaceManager {

  private static final Logger log = LoggerFactory.getLogger(DefaultNamespaceManager.class);
  private final RepositoryManager repositoryManager;
  private final NamespaceDao dao;
  private final ScmEventBus eventBus;
  private final AdministrationContext administrationContext;

  @Inject
  public DefaultNamespaceManager(RepositoryManager repositoryManager, NamespaceDao dao, ScmEventBus eventBus, AdministrationContext administrationContext) {
    this.repositoryManager = repositoryManager;
    this.dao = dao;
    this.eventBus = eventBus;
    this.administrationContext = administrationContext;
  }

  @Override
  public Optional<Namespace> get(String namespace) {
    return repositoryManager
      .getAllNamespaces()
      .stream()
      .filter(n -> n.equals(namespace))
      .map(this::createNamespaceForName)
      .findFirst();
  }

  @Override
  public Collection<Namespace> getAll() {
    return repositoryManager
      .getAllNamespaces()
      .stream()
      .map(this::createNamespaceForName)
      .toList();
  }

  @Override
  public void modify(Namespace namespace) {
    NamespacePermissions.permissionWrite().check(namespace);
    Namespace oldNamespace = get(namespace.getNamespace())
      .orElseThrow(() -> notFound(entity(Namespace.class, namespace.getNamespace())));
    fireEvent(HandlerEventType.BEFORE_MODIFY, namespace, oldNamespace);
    dao.add(namespace);
    fireEvent(HandlerEventType.MODIFY, namespace, oldNamespace);
  }

  @Subscribe(async = false)
  public void handleRepositoryEvent(RepositoryEvent repositoryEvent) {
    if (repositoryRemovedFromNamespace(repositoryEvent)) {
      cleanUpNamespaceIfEmpty(repositoryEvent);
    }
    if (repositoryCreatedInNamespace(repositoryEvent)) {
      initializeIfNeeded(repositoryEvent);
    }
  }

  private static boolean repositoryCreatedInNamespace(RepositoryEvent repositoryEvent) {
    return repositoryEvent.getEventType() == HandlerEventType.CREATE;
  }

  private void cleanUpNamespaceIfEmpty(RepositoryEvent repositoryEvent) {
    String oldNamespace = getOldNamespace(repositoryEvent);
    log.debug("checking whether to delete namespace {} after deletion of repository", oldNamespace);
    administrationContext.runAsAdmin(() -> {
      Collection<String> allNamespaces = repositoryManager.getAllNamespaces();
      if (!allNamespaces.contains(oldNamespace)) {
        log.debug("deleting configuration for namespace {} after deletion of repository", oldNamespace);
        dao.delete(oldNamespace);
      }
    });
  }

  private void initializeIfNeeded(RepositoryEvent repositoryEvent) {
    String creatingUser = SecurityUtils.getSubject().getPrincipal().toString();
    String namespace = repositoryEvent.getItem().getNamespace();
    log.debug("checking whether to set OWNER permissions for user {} in namespace {} after creation of repository", creatingUser, namespace);
    administrationContext.runAsAdmin(() -> {
      Namespace namespaceInstance = createNamespaceForName(namespace);
      if (repositoryManager.getAll(r -> r.getNamespace().equals(namespaceInstance.getNamespace())).size() == 1) {
        log.debug("setting OWNER permissions for user {} in namespace {} after creation of repository", creatingUser, namespace);
        namespaceInstance.addPermission(new RepositoryPermission(creatingUser, "OWNER", false));
        modify(namespaceInstance);
      }
    });
  }

  public boolean repositoryRemovedFromNamespace(RepositoryEvent repositoryEvent) {
    HandlerEventType eventType = repositoryEvent.getEventType();
    return eventType == HandlerEventType.DELETE
      || eventType == HandlerEventType.MODIFY && !repositoryEvent.getItem().getNamespace().equals(repositoryEvent.getOldItem().getNamespace());
  }

  public String getOldNamespace(RepositoryEvent repositoryEvent) {
    if (repositoryEvent.getEventType() == HandlerEventType.DELETE) {
      return repositoryEvent.getItem().getNamespace();
    } else {
      return repositoryEvent.getOldItem().getNamespace();
    }
  }

  private Namespace createNamespaceForName(String namespace) {
    if (NamespacePermissions.permissionRead().isPermitted(namespace)) {
      return dao.get(namespace)
        .map(Namespace::clone)
        .orElse(new Namespace(namespace));
    } else {
      return new Namespace(namespace);
    }
  }

  protected void fireEvent(HandlerEventType event, Namespace namespace, Namespace oldNamespace) {
    eventBus.post(new NamespaceModificationEvent(event, namespace, oldNamespace));
  }
}
