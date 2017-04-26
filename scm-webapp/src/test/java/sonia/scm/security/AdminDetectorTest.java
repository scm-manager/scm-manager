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
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

/**
 * Unit tests for {@link AdminDetector}.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
public class AdminDetectorTest {

  private ScmConfiguration configuration;
  private AdminDetector detector;
  
  @Before
  public void setUpObjectUnderTest(){
    configuration = new ScmConfiguration();
    detector = new AdminDetector(configuration);
  }

  /**
   * Tests {@link AdminDetector#checkForAuthenticatedAdmin(User, Set)} with configured admin users.
   */
  @Test
  public void testCheckForAuthenticatedAdminWithConfiguredAdminUsers() {
    configuration.setAdminUsers(ImmutableSet.of("slarti"));
    
    User slarti = UserTestData.createSlarti();
    slarti.setAdmin(false);
    Set<String> groups = ImmutableSet.of();
    
    detector.checkForAuthenticatedAdmin(slarti, groups);
    assertTrue(slarti.isAdmin());
  }
  
  /**
   * Tests {@link AdminDetector#checkForAuthenticatedAdmin(User, Set)} with configured admin group.
   */
  @Test
  public void testCheckForAuthenticatedAdminWithConfiguredAdminGroup() {
    configuration.setAdminGroups(ImmutableSet.of("heartOfGold"));
    
    User slarti = UserTestData.createSlarti();
    slarti.setAdmin(false);
    Set<String> groups = ImmutableSet.of("heartOfGold");
    
    detector.checkForAuthenticatedAdmin(slarti, groups);
    assertTrue(slarti.isAdmin());
  }

  /**
   * Tests {@link AdminDetector#checkForAuthenticatedAdmin(User, Set)} with non matching configuration.
   */
  @Test
  public void testCheckForAuthenticatedAdminWithNonMatchinConfiguration() {
    configuration.setAdminUsers(ImmutableSet.of("slarti"));
    configuration.setAdminGroups(ImmutableSet.of("heartOfGold"));
    
    User trillian = UserTestData.createTrillian();
    trillian.setAdmin(false);
    Set<String> groups = ImmutableSet.of("puzzle42");
    
    detector.checkForAuthenticatedAdmin(trillian, groups);
    assertFalse(trillian.isAdmin());
  }
  
/**
   * Tests {@link AdminDetector#checkForAuthenticatedAdmin(User, Set)} with user which is already admin.
   */
  @Test
  public void testCheckForAuthenticatedAdminWithUserWhichIsAlreadyAdmin() {
    configuration.setAdminUsers(ImmutableSet.of("slarti"));
    configuration.setAdminGroups(ImmutableSet.of("heartOfGold"));
    
    User trillian = UserTestData.createTrillian();
    trillian.setAdmin(true);
    Set<String> groups = ImmutableSet.of("puzzle42");
    
    detector.checkForAuthenticatedAdmin(trillian, groups);
    assertTrue(trillian.isAdmin());
  }
}