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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.HandlerEventType.CREATE;
import static sonia.scm.HandlerEventType.DELETE;
import static sonia.scm.HandlerEventType.MODIFY;


@ExtendWith(MockitoExtension.class)
class DefaultNamespaceManagerTest {

  @Mock
  RepositoryManager repositoryManager;
  @Mock
  ScmEventBus eventBus;
  @Mock
  AdministrationContext administrationContext;
  @Mock
  Subject subject;

  Namespace life;

  NamespaceDao dao;
  DefaultNamespaceManager manager;
  private Namespace universe;
  private Namespace rest;

  @BeforeEach
  void mockExistingNamespaces() {
    dao = new NamespaceDao(new InMemoryDataStoreFactory(new InMemoryDataStore<Namespace>()));

    when(repositoryManager.getAllNamespaces()).thenReturn(asList("life", "universe", "rest"));

    life = new Namespace("life");
    RepositoryPermission lifePermission = new RepositoryPermission("humans", "OWNER", true);
    life.addPermission(lifePermission);
    dao.add(life);

    universe = new Namespace("universe");
    rest = new Namespace("rest");

    manager = new DefaultNamespaceManager(repositoryManager, dao, eventBus, administrationContext);
  }

  @BeforeEach
  void mockSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldCreateEmptyOptionalIfNamespaceDoesNotExist() {
    Optional<Namespace> namespace = manager.get("dolphins");

    assertThat(namespace).isEmpty();
  }

  @Nested
  class WithAdminContext {
    @BeforeEach
    void mockAdminContext() {
      doAnswer(invocation -> {
        invocation.getArgument(0, Runnable.class).run();
        return null;
      }).when(administrationContext).runAsAdmin(any(PrivilegedAction.class));
    }

    @Test
    void shouldCleanUpPermissionWhenLastRepositoryOfNamespaceWasDeleted() {
      when(repositoryManager.getAllNamespaces()).thenReturn(asList("universe", "rest"));

      manager.handleRepositoryEvent(new RepositoryEvent(DELETE, new Repository("1", "git", "life", "earth")));

      assertThat(dao.get("life")).isEmpty();
    }

    @Test
    void shouldCleanUpPermissionWhenLastRepositoryOfNamespaceWasRenamed() {
      when(repositoryManager.getAllNamespaces()).thenReturn(asList("universe", "rest", "highway"));

      manager.handleRepositoryEvent(
        new RepositoryModificationEvent(
          MODIFY,
          new Repository("1", "git", "highway", "earth"),
          new Repository("1", "git", "life", "earth")));

      assertThat(dao.get("life")).isEmpty();
    }

    @Test
    void shouldCreateOwnerPermissionWhenFirstRepositoryOfNamespaceWasCreated() {
      when(subject.getPrincipal()).thenReturn("trillian");
      when(repositoryManager.getAllNamespaces()).thenReturn(asList("rest", "highway", "universe"));
      when(repositoryManager.getAll(any()))
        .thenAnswer(invocation -> Stream.of(new Repository("1", "git", "universe", "earth")).filter(invocation.getArgument(0, Predicate.class)).toList());
      manager.handleRepositoryEvent(
        new RepositoryModificationEvent(
          CREATE,
          new Repository("1", "git", "universe", "earth"),
          null));

      assertThat(dao.get("universe")).isNotEmpty();
      assertThat(dao.get("universe").get().getPermissions()).extracting("name").contains("trillian");
    }
  }

  @Nested
  class WithPermissionToReadPermissions {

    @BeforeEach
    void grantReadPermission() {
      lenient().when(subject.isPermitted("namespace:permissionRead:life")).thenReturn(true);
    }

    @Test
    void shouldCreateNewNamespaceObjectIfNotInStore() {
      when(subject.isPermitted("namespace:permissionRead:universe")).thenReturn(true);
      Namespace namespace = manager.get("universe").orElse(null);

      assertThat(namespace).isEqualTo(universe);
      assertThat(namespace.getPermissions()).isEmpty();
    }

    @Test
    void shouldEnrichExistingNamespaceWithPermissions() {
      Namespace namespace = manager.get("life").orElse(null);

      assertThat(namespace.getPermissions()).containsExactly(life.getPermissions().toArray(new RepositoryPermission[0]));
    }

    @Test
    void shouldEnrichExistingNamespaceWithPermissionsInGetAll() {
      Collection<Namespace> namespaces = manager.getAll();

      assertThat(namespaces).containsExactly(
        life,
        universe,
        rest
      );
      Namespace foundLifeNamespace = namespaces.stream().filter(namespace -> namespace.getNamespace().equals("life")).findFirst().get();
      assertThat(
        foundLifeNamespace.getPermissions()).containsExactly(life.getPermissions().toArray(new RepositoryPermission[0]));
    }

    @Test
    void shouldModifyExistingNamespaceWithPermissions() {
      Namespace modifiedNamespace = manager.get("life").get();

      modifiedNamespace.setPermissions(asList(new RepositoryPermission("Arthur Dent", "READ", false)));
      manager.modify(modifiedNamespace);

      Namespace newLife = manager.get("life").get();

      assertThat(newLife).isEqualTo(modifiedNamespace);
      verify(eventBus).post(argThat(event -> ((NamespaceModificationEvent) event).getEventType() == HandlerEventType.BEFORE_MODIFY));
      verify(eventBus).post(argThat(event -> ((NamespaceModificationEvent) event).getEventType() == HandlerEventType.MODIFY));
    }
  }

  @Nested
  class WithoutPermissionToReadOrWritePermissions {

    @BeforeEach
    void grantReadPermission() {
      when(subject.isPermitted("namespace:permissionRead:life")).thenReturn(false);
      lenient().doThrow(AuthorizationException.class).when(subject).checkPermission("namespace:permissionWrite:life");
    }

    @Test
    void shouldNotEnrichExistingNamespaceWithPermissions() {
      Namespace namespace = manager.get("life").orElse(null);

      assertThat(namespace.getPermissions()).isEmpty();
    }

    @Test
    void shouldNotEnrichExistingNamespaceWithPermissionsInGetAll() {
      Collection<Namespace> namespaces = manager.getAll();

      assertThat(namespaces).containsExactly(
        new Namespace("life"),
        universe,
        rest
      );
    }

    @Test
    void shouldNotModifyExistingNamespaceWithPermissions() {
      Namespace modifiedNamespace = manager.get("life").get();

      modifiedNamespace.setPermissions(asList(new RepositoryPermission("Arthur Dent", "READ", false)));

      Assertions.assertThrows(AuthorizationException.class, () -> manager.modify(modifiedNamespace));
    }
  }
}
