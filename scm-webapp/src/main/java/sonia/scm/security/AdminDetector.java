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

import com.google.inject.Inject;
import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.user.User;
import sonia.scm.util.Util;

/**
 * Detects administrator from configuration.
 *
 * @author Sebastian Sdorra
 * @since 1.52
 */
public class AdminDetector {
  
  /**
   * the logger for AdminDetector
   */
  private static final Logger LOG = LoggerFactory.getLogger(AdminDetector.class);
  
  private final ScmConfiguration configuration;

  /**
   * Constructs admin detector.
   * 
   * @param configuration scm-manager main configuration
   */
  @Inject
  public AdminDetector(ScmConfiguration configuration) {
    this.configuration = configuration;
  }
  
  /**
   * Checks is the authenticated user is marked as administrator by {@link ScmConfiguration}.
   * 
   * @param user authenticated user
   * @param groups groups of authenticated user
   */
  public void checkForAuthenticatedAdmin(User user, Set<String> groups) {
    if (!user.isAdmin()) {
      user.setAdmin(isAdminByConfiguration(user, groups));

      if (LOG.isDebugEnabled() && user.isAdmin()) {
        LOG.debug("user {} is marked as admin by configuration", user.getName());
      }
    }
    else if (LOG.isDebugEnabled()) {
      LOG.debug("authenticator {} marked user {} as admin", user.getType(), user.getName());
    }
  }
  
  private boolean isAdminByConfiguration(User user, Collection<String> groups) {
    boolean result = false;
    
    Set<String> adminUsers = configuration.getAdminUsers();
    if (adminUsers != null) {
      result = adminUsers.contains(user.getName());
    }

    if (!result) {
      Set<String> adminGroups = configuration.getAdminGroups();

      if (adminGroups != null) {
        result = Util.containsOne(adminGroups, groups);
      }
    }

    return result;
  }
  
}
