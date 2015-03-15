/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.security.AuthorizationCollector;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.Role;
import sonia.scm.security.SecuritySystem;
import sonia.scm.security.StringablePermission;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Factory to create {@link ScmState}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class ScmStateFactory
{

  /**
   * Constructs a new {@link ScmStateFactory}.
   *
   *
   * @param contextProvider context provider
   * @param configuration configuration
   * @param repositoryManger repository manager
   * @param userManager user manager
   * @param securitySystem security system
   * @param authorizationCollector authorization collector
   */
  @Inject
  public ScmStateFactory(SCMContextProvider contextProvider,
    ScmConfiguration configuration, RepositoryManager repositoryManger,
    UserManager userManager, SecuritySystem securitySystem,
    AuthorizationCollector authorizationCollector)
  {
    this.contextProvider = contextProvider;
    this.configuration = configuration;
    this.repositoryManger = repositoryManger;
    this.userManager = userManager;
    this.securitySystem = securitySystem;
    this.authorizationCollector = authorizationCollector;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns anonymous state.
   *
   *
   * @return anonymous state
   */
  @SuppressWarnings("unchecked")
  public ScmState createAnonymousState()
  {
    return createState(SCMContext.ANONYMOUS, Collections.EMPTY_LIST, null,
      Collections.EMPTY_LIST, Collections.EMPTY_LIST);
  }

  /**
   * Creates an state from the given subject.
   *
   *
   * @param subject subject
   *
   * @return state from subject
   */
  public ScmState createState(Subject subject)
  {
    return createState(subject, null);
  }

  /**
   * Creates an state from the given subject and authentication token.
   *
   *
   * @param subject subject
   * @param token authentication token
   *
   * @return state from subject and authentication token
   */
  @SuppressWarnings("unchecked")
  public ScmState createState(Subject subject, String token)
  {
    PrincipalCollection collection = subject.getPrincipals();
    User user = collection.oneByType(User.class);
    GroupNames groups = collection.oneByType(GroupNames.class);

    List<PermissionDescriptor> ap = Collections.EMPTY_LIST;

    if (subject.hasRole(Role.ADMIN))
    {
      ap = securitySystem.getAvailablePermissions();
    }

    Builder<String> builder = ImmutableList.builder();

    for (Permission p : authorizationCollector.collect().getObjectPermissions())
    {
      if (p instanceof StringablePermission)
      {
        builder.add(((StringablePermission) p).getAsString());
      }

    }

    return createState(user, groups.getCollection(), token, builder.build(),
      ap);
  }

  private ScmState createState(User user, Collection<String> groups,
    String token, List<String> assignedPermissions,
    List<PermissionDescriptor> availablePermissions)
  {
    User u = user.clone();
    // do not return password on authentication
    u.setPassword(null);
    return new ScmState(contextProvider.getVersion(), u, groups, token,
      repositoryManger.getConfiguredTypes(), userManager.getDefaultType(),
      new ScmClientConfig(configuration), assignedPermissions,
      availablePermissions);
  }

  //~--- fields ---------------------------------------------------------------

  /** authorization collector */
  private final AuthorizationCollector authorizationCollector;

  /** configuration */
  private final ScmConfiguration configuration;

  /** context provider */
  private final SCMContextProvider contextProvider;

  /** repository manager */
  private final RepositoryManager repositoryManger;

  /** security system */
  private final SecuritySystem securitySystem;

  /** user manager */
  private final UserManager userManager;
}
