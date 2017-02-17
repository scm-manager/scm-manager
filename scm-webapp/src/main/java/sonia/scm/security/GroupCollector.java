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

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupNames;
import sonia.scm.user.User;
import sonia.scm.util.Util;
import sonia.scm.web.security.AuthenticationResult;

/**
 * Collects groups from {@link GroupManager} after authentication.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
public class GroupCollector {
  
  /**
   * the logger for GroupCollector
   */
  private static final Logger LOG = LoggerFactory.getLogger(GroupCollector.class);
 
  private final GroupManager groupManager;

  @Inject
  public GroupCollector(GroupManager groupManager) {
    this.groupManager = groupManager;
  }
  
  /**
   * Collect groups from {@link AuthenticationResult} and {@link GroupManager}.
   * 
   * @param authenticationResult authentication result
   * 
   * @return collected groups
   */
  public Set<String> collectGroups(AuthenticationResult authenticationResult) {
    Set<String> groups = Sets.newHashSet();

    // add group for all authenticated users
    groups.add(GroupNames.AUTHENTICATED);

    // load external groups
    Collection<String> groupsFromAuthenticator = authenticationResult.getGroups();

    if (groupsFromAuthenticator != null) {
      groups.addAll(groupsFromAuthenticator);
    }

    User user = authenticationResult.getUser();
    loadGroupFromDatabase(user, groups);

    if (LOG.isDebugEnabled()) {
      LOG.debug(createMembershipLogMessage(user, groups));
    }
    
    return groups;
  }

  private void loadGroupFromDatabase(User user, Set<String> groupSet) {
    Collection<Group> groupCollection = groupManager.getGroupsForMember(user.getName());

    if (groupCollection != null) {
      for (Group group : groupCollection) {
        groupSet.add(group.getName());
      }
    }
  }
  
  private String createMembershipLogMessage(User user, Set<String> groups) {
    StringBuilder msg = new StringBuilder("user ");

    msg.append(user.getName());

    if (Util.isNotEmpty(groups)) {
      msg.append(" is member of ");

      Joiner.on(", ").appendTo(msg, groups);
    } else {
      msg.append(" is not a member of a group");
    }
    
    return msg.toString();
  }
  
}
