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

import org.apache.shiro.authz.permission.PermissionResolver;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.HandlerEvent;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.group.Group;
import sonia.scm.group.GroupEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserTestData;

/**
 * Unit tests for {@link AuthorizationCollector}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthorizationCollectorTest {

  @Mock
  private Cache cache;
  
  @Mock
  private CacheManager cacheManager;

  @Mock
  private RepositoryDAO repositoryDAO;

  @Mock
  private PermissionResolver resolver;

  @Mock
  private SecuritySystem securitySystem;
  
  private AuthorizationCollector collector;
  
  /**
   * Set up object to test.
   */
  @Before
  public void setUp(){
    when(cacheManager.getCache(Mockito.any(Class.class), Mockito.any(Class.class), Mockito.any(String.class)))
      .thenReturn(cache);
    
    collector = new AuthorizationCollector(cacheManager, repositoryDAO, securitySystem, resolver);
  }

  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.user.UserEvent)}.
   */
  @Test
  public void testOnUserEvent()
  {
    User user = UserTestData.createDent();
    collector.onEvent(new UserEvent(user, HandlerEvent.BEFORE_CREATE));
    verify(cache, never()).clear();
    
    collector.onEvent(new UserEvent(user, HandlerEvent.CREATE));
    verify(cache).clear();
  }
  
  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.group.GroupEvent)}.
   */
  @Test
  public void testOnGroupEvent()
  {
    Group group = new Group("xml", "base");
    collector.onEvent(new GroupEvent(group, HandlerEvent.BEFORE_CREATE));
    verify(cache, never()).clear();
    
    collector.onEvent(new GroupEvent(group, HandlerEvent.CREATE));
    verify(cache).clear();
  }
  
  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.repository.RepositoryEvent)}.
   */
  @Test
  public void testOnRepositoryEvent()
  {
    Repository repository = RepositoryTestData.createHeartOfGold();
    collector.onEvent(new RepositoryEvent(repository, HandlerEvent.BEFORE_CREATE));
    verify(cache, never()).clear();
    
    collector.onEvent(new RepositoryEvent(repository, HandlerEvent.CREATE));
    verify(cache).clear();
  }
  
  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.security.StoredAssignedPermissionEvent)}.
   */
  @Test
  public void testOnStoredAssignedPermissionEvent()
  {
    StoredAssignedPermission permission = new StoredAssignedPermission();
    collector.onEvent(new StoredAssignedPermissionEvent(HandlerEvent.BEFORE_CREATE, permission));
    verify(cache, never()).clear();
    
    collector.onEvent(new StoredAssignedPermissionEvent(HandlerEvent.CREATE, permission));
    verify(cache).clear();
  }

}