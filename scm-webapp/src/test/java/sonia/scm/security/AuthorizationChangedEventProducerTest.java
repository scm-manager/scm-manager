/**
 * Copyright (c) 2014, Sebastian Sdorra
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

package sonia.scm.security;

import com.google.common.collect.Lists;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import sonia.scm.HandlerEventType;
import sonia.scm.group.Group;
import sonia.scm.group.GroupEvent;
import sonia.scm.group.GroupModificationEvent;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserModificationEvent;
import sonia.scm.user.UserTestData;

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
    assertTrue(producer.event.isEveryUserAffected());
    assertEquals(username, producer.event.getNameOfAffectedUser());
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
    
    userModified.setAdmin(true);
    
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
  
  private void assertGlobalEventIsFired(){
    assertNotNull(producer.event);
    assertFalse(producer.event.isEveryUserAffected());
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
    repositoryModified.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("test")));
    
    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("test")));
    
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.BEFORE_CREATE, repositoryModified, repository));
    assertEventIsNotFired();
    
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertEventIsNotFired();
    
    repositoryModified.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("test")));
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertEventIsNotFired();
    
    repositoryModified.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("test123")));
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertGlobalEventIsFired();
    
    resetStoredEvent();

    repositoryModified.setPermissions(
      Lists.newArrayList(new sonia.scm.repository.Permission("test", PermissionType.READ, true))
    );
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertGlobalEventIsFired();
    
    resetStoredEvent();
    
    repositoryModified.setPermissions(
      Lists.newArrayList(new sonia.scm.repository.Permission("test", PermissionType.WRITE))
    );
    producer.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    assertGlobalEventIsFired();
  }
  
  private void resetStoredEvent(){
    producer.event = null;
  }
  
  /**
   * Tests {@link AuthorizationChangedEventProducer#onEvent(sonia.scm.security.StoredAssignedPermissionEvent)}.
   */
  @Test
  public void testOnStoredAssignedPermissionEvent()
  {
    StoredAssignedPermission groupPermission = new StoredAssignedPermission(
      "123", new AssignedPermission("_authenticated", true, "repository:read:*")
    );
    producer.onEvent(new StoredAssignedPermissionEvent(HandlerEventType.BEFORE_CREATE, groupPermission));
    assertEventIsNotFired();
    
    producer.onEvent(new StoredAssignedPermissionEvent(HandlerEventType.CREATE, groupPermission));
    assertGlobalEventIsFired();
    
    resetStoredEvent();
    
    StoredAssignedPermission userPermission = new StoredAssignedPermission(
      "123", new AssignedPermission("trillian", false, "repository:read:*")
    );
    producer.onEvent(new StoredAssignedPermissionEvent(HandlerEventType.BEFORE_CREATE, userPermission));
    assertEventIsNotFired();
    
    resetStoredEvent();
    
    producer.onEvent(new StoredAssignedPermissionEvent(HandlerEventType.CREATE, userPermission));
    assertUserEventIsFired("trillian");
  }
  
  private static class StoringAuthorizationChangedEventProducer extends AuthorizationChangedEventProducer {
  
    private AuthorizationChangedEvent event;

    @Override
    protected void sendEvent(AuthorizationChangedEvent event) {
      this.event = event;
    }
    
  }

}