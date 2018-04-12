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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.user.UserTestData;
import sonia.scm.web.security.AuthenticationResult;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupCollector}.
 *
 * @author Sebastian Sdorra
 * @since 1.52
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupCollectorTest {

  @Mock
  private GroupManager groupManager;

  @InjectMocks
  private GroupCollector collector;

  /**
   * Tests {@link GroupCollector#collectGroups(AuthenticationResult)} without groups from authenticator.
   */
  @Test
  public void testCollectGroupsWithoutAuthenticatorGroups() {
    Set<String> groups = collector.collectGroups(new AuthenticationResult(UserTestData.createSlarti()));
    assertThat(groups, containsInAnyOrder("_authenticated"));
  }

  /**
   * Tests {@link GroupCollector#collectGroups(AuthenticationResult)} with groups from authenticator.
   */
  @Test
  public void testCollectGroupsWithGroupsFromAuthenticator() {
    Set<String> authGroups = ImmutableSet.of("puzzle42");
    Set<String> groups = collector.collectGroups(new AuthenticationResult(UserTestData.createSlarti(), authGroups));
    assertThat(groups, containsInAnyOrder("_authenticated", "puzzle42"));
  }

  /**
   * Tests {@link GroupCollector#collectGroups(AuthenticationResult)} with groups from db.
   */
  @Test
  public void testCollectGroupsWithGroupsFromDB() {
    Set<Group> dbGroups = ImmutableSet.of(new Group("test", "puzzle42"));
    when(groupManager.getGroupsForMember("slarti")).thenReturn(dbGroups);
    Set<String> groups = collector.collectGroups(new AuthenticationResult(UserTestData.createSlarti()));
    assertThat(groups, containsInAnyOrder("_authenticated", "puzzle42"));
  }

/**
   * Tests {@link GroupCollector#collectGroups(AuthenticationResult)} with groups from db.
   */
  @Test
  public void testCollectGroupsWithGroupsFromDBAndAuthenticator() {
    Set<Group> dbGroups = ImmutableSet.of(new Group("test", "puzzle42"));
    Set<String> authGroups = ImmutableSet.of("heartOfGold");
    when(groupManager.getGroupsForMember("slarti")).thenReturn(dbGroups);
    Set<String> groups = collector.collectGroups(new AuthenticationResult(UserTestData.createSlarti(), authGroups));
    assertThat(groups, containsInAnyOrder("_authenticated", "puzzle42", "heartOfGold"));
  }

}
