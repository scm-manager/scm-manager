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
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;

/**
 * Unit tests for {@link LocalDatabaseSynchronizer}.
 *
 * @author Sebastian Sdorra
 * @since 1.52
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalDatabaseSynchronizerTest {

  @Mock
  private AdminDetector adminSelector;
  
  @Mock
  private UserManager userManager;
  
  @Mock
  private UserDAO userDAO;
  
  @InjectMocks
  private LocalDatabaseSynchronizer synchronizer;

  /**
   * Tests {@link LocalDatabaseSynchronizer#synchronize(User, java.util.Set)}.
   */
  @Test
  public void testSynchronizeWithoutDBUser() {
    User trillian = UserTestData.createTrillian();
    trillian.setType("local");
    synchronizer.synchronize(trillian, ImmutableSet.<String>of());
    verify(userDAO).add(trillian);
  }
  
  /**
   * Tests {@link LocalDatabaseSynchronizer#synchronize(sonia.scm.user.User, java.util.Set)}.
   */
  @Test
  public void testSynchronize() {
    User trillian = UserTestData.createTrillian();
    trillian.setDisplayName("Trici");
    trillian.setType("local");
    trillian.setAdmin(false);
    trillian.setActive(true);
    
    User dbTrillian = UserTestData.createTrillian();
    dbTrillian.setType("local");
    dbTrillian.setAdmin(true);
    dbTrillian.setActive(false);
    
    when(userDAO.get(trillian.getId())).thenReturn(dbTrillian);
    
    synchronizer.synchronize(trillian, ImmutableSet.<String>of());
    assertTrue(trillian.isAdmin());
    assertFalse(trillian.isActive());
    verify(userDAO).modify(trillian);
  }
  
  

}