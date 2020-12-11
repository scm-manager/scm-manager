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

package sonia.scm.security;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.HandlerEventType;
import sonia.scm.group.Group;
import sonia.scm.group.GroupEvent;
import sonia.scm.group.GroupModificationEvent;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceModificationEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserModificationEvent;
import sonia.scm.user.UserTestData;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link AuthorizationChangedEventProducer}.
 *
 * @author Sebastian Sdorra
 */
public class AuthorizationChangedEventProducerTest {

  private StoringAuthorizationChangedEventProducer producer;

  @Before
  public void setUpProducer() {
     producer = new StoringAuthorizationChangedEventProducer();
  }

  /**
   * Tests {@link AuthorizationChangedEventProducer#onEvent(sonia.scm.user.UserEvent)}.
   */
  @Test
  public void testOnUserEvent()
  {
    User user = UserTestData.createDent();
    producer.onEvent(new UserEvent(HandlerEventType.BEFORE_CREATE, user));
    assertEventIsNotFired();

    producer.onEvent(new UserEvent(HandlerEventType.CREATE, user));
    assertUserEventIsFired("dent");
  }

  private void assertEventIsNotFired(){
    assertNull(producer.event);
  }

  private void assertUserEventIsFired(String username){
    assertNotNull(producer.event);
    assertFalse(producer.event.isEveryUserAffected());
    assertEquals(username, producer.event.getNameOfAffectedUser());
  }

  private void assertGlobalEventIsFired(){
    assertNotNull(producer.event);
    assertTrue(producer.event.isEveryUserAffected());
  }

  /**
   * Tests {@link AuthorizationChangedEventProducer#onEvent(sonia.scm.user.UserEvent)} with modified user.
   */
  @Test
  public void testOnUserModificationEvent()
  {
    User user = UserTestData.createDent();
    User userModified = UserTestData.createDent();
    userModified.setDisplayName("Super Dent");

    producer.onEvent(new UserModificationEvent(HandlerEventType.BEFORE_CREATE, userModified, user));
    assertEventIsNotFired();

    producer.onEvent(new UserModificationEvent(HandlerEventType.CREATE, userModified, user));
    assertEventIsNotFired();

    userModified.setActive(false);

    producer.onEvent(new UserModificationEvent(HandlerEventType.BEFORE_CREATE, userModified, user));
    assertEventIsNotFired();

    producer.onEvent(new UserModificationEvent(HandlerEventType.CREATE, userModified, user));
    assertUserEventIsFired("dent");
  }

  /**
   * Tests {@link AuthorizationChangedEventProducer#onEvent(sonia.scm.group.GroupEvent)}.
   */
  @Test
  public void testOnGroupEvent()
  {
    Group group = new Group("xml", "base");
    producer.onEvent(new GroupEvent(HandlerEventType.BEFORE_CREATE, group));
    assertEventIsNotFired();

    producer.onEvent(new GroupEvent(HandlerEventType.CREATE, group));
    assertGlobalEventIsFired();
  }

  /**
   * Tests {@link AuthorizationChangedEventProducer#onEvent(sonia.scm.group.GroupEvent)} with modified groups.
   */
  @Test
  public void testOnGroupModificationEvent()
  {
    Group group = new Group("xml", "base");
    Group modifiedGroup = new Group("xml", "base");
    producer.onEvent(new GroupModificationEvent(HandlerEventType.BEFORE_MODIFY, modifiedGroup, group));
    assertEventIsNotFired();

    producer.onEvent(new GroupModificationEvent(HandlerEventType.MODIFY, modifiedGroup, group));
    assertEventIsNotFired();

    modifiedGroup.add("test");
    producer.onEvent(new GroupModificationEvent(HandlerEventType.MODIFY, modifiedGroup, group));
    assertGlobalEventIsFired();
  }

  /**
   * Tests {@link AuthorizationChangedEventProducer#onEvent(sonia.scm.repository.RepositoryEvent)}.
   */
  @Test
  public void testOnRepositoryEvent()
  {
    Repository repository = RepositoryTestData.createHeartOfGold();
    producer.onEvent(new RepositoryEvent(HandlerEventType.BEFORE_CREATE, repository));
    assertEventIsNotFired();

    producer.onEvent(new RepositoryEvent(HandlerEventType.CREATE, repository));
    assertGlobalEventIsFired();
  }

 /**
   * Tests {@link AuthorizationChangedEventProducer#onEvent(sonia.scm.repository.RepositoryEvent)} with modified
   * repository.
   */
  @Test
  public void testOnRepositoryModificationEvent()
  {
    Repository repositoryModified = RepositoryTestData.createHeartOfGold();
    repositoryModified.setName("test123");
    repositoryModified.setPermissions(Lists.newArrayList(new RepositoryPermission("test", singletonList("read"), false)));

    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setPermissions(Lists.newArrayList(new RepositoryPermission("test", singletonList("read"), false)));

    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.BEFORE_CREATE, repositoryModified, repository));
    assertEventIsNotFired();

    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertEventIsNotFired();

    repositoryModified.setPermissions(Lists.newArrayList(new RepositoryPermission("test", singletonList("read"), false)));
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertEventIsNotFired();

    repositoryModified.setPermissions(Lists.newArrayList(new RepositoryPermission("test123", singletonList("read"), false)));
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertGlobalEventIsFired();

    resetStoredEvent();

    repositoryModified.setPermissions(
      Lists.newArrayList(new RepositoryPermission("test", singletonList("read"), true))
    );
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertGlobalEventIsFired();

    resetStoredEvent();

    repositoryModified.setPermissions(
      Lists.newArrayList(new RepositoryPermission("test", asList("read", "write"), false))
    );
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertGlobalEventIsFired();

    resetStoredEvent();
    repository.setPermissions(Lists.newArrayList(new RepositoryPermission("test", asList("read", "write"), false)));

    repositoryModified.setPermissions(
      Lists.newArrayList(new RepositoryPermission("test", asList("write", "read"), false))
    );
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertEventIsNotFired();
  }

  @Test
  public void testOnRepositoryNamespaceChanged()
  {
    Repository repositoryModified = RepositoryTestData.createHeartOfGold();
    repositoryModified.setName("test123");
    Repository repository = RepositoryTestData.createHeartOfGold();

    repositoryModified.setNamespace("new_namespace");
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertGlobalEventIsFired();
  }

  private void resetStoredEvent(){
    producer.event = null;
  }

  /**
   * Tests {@link AuthorizationChangedEventProducer#onEvent(AssignedPermissionEvent)}.
   */
  @Test
  public void testOnStoredAssignedPermissionEvent()
  {
    StoredAssignedPermission groupPermission = new StoredAssignedPermission(
      "123", new AssignedPermission("_authenticated", true, "repository:read:*")
    );
    producer.onEvent(new AssignedPermissionEvent(HandlerEventType.BEFORE_CREATE, groupPermission));
    assertEventIsNotFired();

    producer.onEvent(new AssignedPermissionEvent(HandlerEventType.CREATE, groupPermission));
    assertGlobalEventIsFired();

    resetStoredEvent();

    StoredAssignedPermission userPermission = new StoredAssignedPermission(
      "123", new AssignedPermission("trillian", false, "repository:read:*")
    );
    producer.onEvent(new AssignedPermissionEvent(HandlerEventType.BEFORE_CREATE, userPermission));
    assertEventIsNotFired();

    resetStoredEvent();

    producer.onEvent(new AssignedPermissionEvent(HandlerEventType.CREATE, userPermission));
    assertUserEventIsFired("trillian");
  }

  @Test
  public void testOnNamespaceModificationEvent()
  {
    Namespace namespaceModified = new Namespace("hitchhiker");
    namespaceModified.setPermissions(Lists.newArrayList(new RepositoryPermission("test", singletonList("read"), false)));

    Namespace namespace = new Namespace("hitchhiker");
    namespace.setPermissions(Lists.newArrayList(new RepositoryPermission("test", singletonList("read"), false)));

    producer.onEvent(new NamespaceModificationEvent(HandlerEventType.BEFORE_CREATE, namespaceModified, namespace));
    assertEventIsNotFired();

    producer.onEvent(new NamespaceModificationEvent(HandlerEventType.CREATE, namespaceModified, namespace));
    assertEventIsNotFired();

    namespaceModified.setPermissions(Lists.newArrayList(new RepositoryPermission("test", singletonList("read"), false)));
    producer.onEvent(new NamespaceModificationEvent(HandlerEventType.CREATE, namespaceModified, namespace));
    assertEventIsNotFired();

    namespaceModified.setPermissions(Lists.newArrayList(new RepositoryPermission("test123", singletonList("read"), false)));
    producer.onEvent(new NamespaceModificationEvent(HandlerEventType.CREATE, namespaceModified, namespace));
    assertGlobalEventIsFired();

    resetStoredEvent();

    namespaceModified.setPermissions(
      Lists.newArrayList(new RepositoryPermission("test", singletonList("read"), true))
    );
    producer.onEvent(new NamespaceModificationEvent(HandlerEventType.CREATE, namespaceModified, namespace));
    assertGlobalEventIsFired();

    resetStoredEvent();

    namespaceModified.setPermissions(
      Lists.newArrayList(new RepositoryPermission("test", asList("read", "write"), false))
    );
    producer.onEvent(new NamespaceModificationEvent(HandlerEventType.CREATE, namespaceModified, namespace));
    assertGlobalEventIsFired();

    resetStoredEvent();
    namespace.setPermissions(Lists.newArrayList(new RepositoryPermission("test", asList("read", "write"), false)));

    namespaceModified.setPermissions(
      Lists.newArrayList(new RepositoryPermission("test", asList("write", "read"), false))
    );
    producer.onEvent(new NamespaceModificationEvent(HandlerEventType.CREATE, namespaceModified, namespace));
    assertEventIsNotFired();
  }

  private static class StoringAuthorizationChangedEventProducer extends AuthorizationChangedEventProducer {

    private AuthorizationChangedEvent event;

    @Override
    protected void sendEvent(AuthorizationChangedEvent event) {
      this.event = event;
    }

  }

}
