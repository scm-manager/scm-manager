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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.group.GroupNames;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.web.security.AuthenticationResult;

/**
 * Unit tests for {@link AuthenticationInfoCollector}.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationInfoCollectorTest {

  @Mock
  private LocalDatabaseSynchronizer synchronizer; 
  
  @Mock
  private GroupCollector groupCollector; 
  
  @Mock
  private SessionStore sessionStore;
  
  @InjectMocks
  private AuthenticationInfoCollector collector;

  /**
   * Tests {@link AuthenticationInfoCollector#createAuthenticationInfo(UsernamePasswordToken, AuthenticationResult)}.
   */
  @Test
  public void testCreateAuthenticationInfo() {
    User trillian = UserTestData.createTrillian();
    UsernamePasswordToken token = new UsernamePasswordToken(trillian.getId(), "secret");
    AuthenticationResult result = new AuthenticationResult(trillian);
    Set<String> groups = ImmutableSet.of("puzzle42", "heartOfGold");
    when(groupCollector.collectGroups(Mockito.any(AuthenticationResult.class))).thenReturn(groups);

    AuthenticationInfo authc = collector.createAuthenticationInfo(token, result);
    verify(synchronizer).synchronize(trillian, groups);
    verify(sessionStore).store(token);
    
    assertEquals(trillian.getId(), authc.getPrincipals().getPrimaryPrincipal());
    assertEquals(trillian, authc.getPrincipals().oneByType(User.class));
    assertThat(authc.getPrincipals().oneByType(GroupNames.class), contains(groups.toArray(new String[0])));
  }
  
  /**
   * Tests {@link AuthenticationInfoCollector#createAuthenticationInfo(UsernamePasswordToken, AuthenticationResult)} 
   * with disabled user.
   */
  @Test(expected = DisabledAccountException.class)
  public void testCreateAuthenticationInfoWithDisabledUser() {
    User trillian = UserTestData.createTrillian();
    trillian.setActive(false);
    UsernamePasswordToken token = new UsernamePasswordToken(trillian.getId(), "secret");
    AuthenticationResult result = new AuthenticationResult(trillian);
    Set<String> groups = ImmutableSet.of("puzzle42", "heartOfGold");
    when(groupCollector.collectGroups(Mockito.any(AuthenticationResult.class))).thenReturn(groups);

    collector.createAuthenticationInfo(token, result);
  }

}