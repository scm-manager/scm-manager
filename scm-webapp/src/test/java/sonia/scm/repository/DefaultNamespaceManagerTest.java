/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository;

import com.github.legman.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.util.Collection;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DefaultNamespaceManagerTest {

  @Mock
  RepositoryManager repositoryManager;
  @Mock
  EventBus eventBus;

  Namespace life;

  NamespaceDao dao;
  DefaultNamespaceManager manager;
  private Namespace universe;
  private Namespace rest;

  @BeforeEach
  void mockExistingNamespaces() {
    dao = new NamespaceDao(new InMemoryDataStoreFactory(new InMemoryDataStore()));
    manager = new DefaultNamespaceManager(repositoryManager, dao, eventBus);

    when(repositoryManager.getAllNamespaces()).thenReturn(asList("life", "universe", "rest"));

    life = new Namespace("life");
    RepositoryPermission lifePermission = new RepositoryPermission("humans", "OWNER", true);
    life.addPermission(lifePermission);
    dao.add(life);

    universe = new Namespace("universe");
    rest = new Namespace("rest");
  }

  @Test
  void shouldCreateEmptyOptionalIfNamespaceDoesNotExist() {
    Optional<Namespace> namespace = manager.get("dolphins");

    assertThat(namespace).isEmpty();
  }

  @Test
  void shouldCreateNewNamespaceObjectIfNotInStore() {
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
    verify(eventBus).post(argThat(event -> ((NamespaceModificationEvent)event).getEventType() == HandlerEventType.BEFORE_MODIFY));
    verify(eventBus).post(argThat(event -> ((NamespaceModificationEvent)event).getEventType() == HandlerEventType.MODIFY));
  }
}
