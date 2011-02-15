/**
 * Copyright (c) 2010, Sebastian Sdorra
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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import sonia.scm.security.ScmSecurityException;
import sonia.scm.user.User;
import sonia.scm.web.security.WebSecurityContext;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class PermissionUtilTest
{

  /**
   * Constructs ...
   *
   */
  public PermissionUtilTest()
  {
    repository = new Repository();
    admams.getUser().setAdmin(true);

    Permission[] permissions = new Permission[] {
                                 new Permission("dent", PermissionType.READ),
                                 new Permission("perfect",
                                   PermissionType.WRITE),
                                 new Permission("marvin",
                                   PermissionType.OWNER) };

    repository.setPermissions(Arrays.asList(permissions));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test(expected = ScmSecurityException.class)
  public void assertFailedPermissionTest()
  {
    PermissionUtil.assertPermission(repository, dent, PermissionType.WRITE);
  }

  /**
   * Method description
   *
   */
  @Test
  public void assertPermissionTest()
  {
    PermissionUtil.assertPermission(repository, dent, PermissionType.READ);
    PermissionUtil.assertPermission(repository, perfect, PermissionType.READ);
    PermissionUtil.assertPermission(repository, perfect, PermissionType.WRITE);
    PermissionUtil.assertPermission(repository, marvin, PermissionType.READ);
    PermissionUtil.assertPermission(repository, marvin, PermissionType.WRITE);
    PermissionUtil.assertPermission(repository, marvin, PermissionType.OWNER);
    PermissionUtil.assertPermission(repository, admams, PermissionType.READ);
    PermissionUtil.assertPermission(repository, admams, PermissionType.WRITE);
    PermissionUtil.assertPermission(repository, admams, PermissionType.OWNER);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGroupPermissions()
  {
    WebSecurityContext dent = mockGroupCtx(new User("dent", "Arthur Dent",
                                "arthur.dent@hitchhiker.com"), "devel", "qa");
    WebSecurityContext ford = mockGroupCtx(new User("ford", "Ford Prefect",
                                "ford.prefect@hitchhiker.com"), "devel");
    WebSecurityContext zaphod = mockGroupCtx(new User("zaphod",
                                  "Zaphod Beeblebrox",
                                  "zaphod.beeblebrox@hitchhiker.com"), "qa");
    WebSecurityContext trillian = mockGroupCtx(new User("trillian",
                                    "Trillian Astra",
                                    "trillian.astra@hitchhiker.com"));
    Repository r = new Repository();

    r.setPermissions(
        new ArrayList<Permission>(
            Arrays.asList(
              new Permission("dent"),
              new Permission("devel", PermissionType.WRITE, true),
              new Permission("qa", PermissionType.READ, true))));

    // member of both devel and qa
    assertTrue(PermissionUtil.hasPermission(r, dent, PermissionType.READ));
    assertTrue(PermissionUtil.hasPermission(r, dent, PermissionType.WRITE));
    assertFalse(PermissionUtil.hasPermission(r, dent, PermissionType.OWNER));

    // now, additionally the owner
    r.getPermissions().add(new Permission("dent", PermissionType.OWNER));
    assertTrue(PermissionUtil.hasPermission(r, dent, PermissionType.OWNER));

    // member of just devel
    assertTrue(PermissionUtil.hasPermission(r, ford, PermissionType.READ));
    assertTrue(PermissionUtil.hasPermission(r, ford, PermissionType.WRITE));
    assertFalse(PermissionUtil.hasPermission(r, ford, PermissionType.OWNER));

    // member of just qa
    assertTrue(PermissionUtil.hasPermission(r, zaphod, PermissionType.READ));
    assertFalse(PermissionUtil.hasPermission(r, zaphod, PermissionType.WRITE));
    assertFalse(PermissionUtil.hasPermission(r, zaphod, PermissionType.OWNER));

    // member of no groups
    assertFalse(PermissionUtil.hasPermission(r, trillian, PermissionType.READ));
    assertFalse(PermissionUtil.hasPermission(r, trillian,
            PermissionType.WRITE));
    assertFalse(PermissionUtil.hasPermission(r, trillian,
            PermissionType.OWNER));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void hasPermissionTest()
  {
    assertTrue(PermissionUtil.hasPermission(repository, dent,
            PermissionType.READ));
    assertTrue(PermissionUtil.hasPermission(repository, perfect,
            PermissionType.READ));
    assertTrue(PermissionUtil.hasPermission(repository, perfect,
            PermissionType.WRITE));
    assertFalse(PermissionUtil.hasPermission(repository, dent,
            PermissionType.WRITE));
    assertFalse(PermissionUtil.hasPermission(repository, slarti,
            PermissionType.WRITE));
    assertFalse(PermissionUtil.hasPermission(repository, slarti,
            PermissionType.READ));
    assertTrue(PermissionUtil.hasPermission(repository, marvin,
            PermissionType.READ));
    assertTrue(PermissionUtil.hasPermission(repository, marvin,
            PermissionType.WRITE));
    assertTrue(PermissionUtil.hasPermission(repository, marvin,
            PermissionType.OWNER));
    assertTrue(PermissionUtil.hasPermission(repository, admams,
            PermissionType.READ));
    assertTrue(PermissionUtil.hasPermission(repository, admams,
            PermissionType.WRITE));
    assertTrue(PermissionUtil.hasPermission(repository, admams,
            PermissionType.OWNER));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  private WebSecurityContext mockCtx(User user)
  {
    WebSecurityContext context = mock(WebSecurityContext.class);

    when(context.getUser()).thenReturn(user);

    return context;
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param groups
   *
   * @return
   */
  private WebSecurityContext mockGroupCtx(User user, String... groups)
  {
    WebSecurityContext context = mockCtx(user);
    Set<String> groupSet = new HashSet<String>(Arrays.asList(groups));

    when(context.getGroups()).thenReturn(groupSet);

    return context;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private WebSecurityContext dent = mockCtx(new User("dent", "Arthur Dent",
                                      "arthur.dent@hitchhiker.com"));

  /** Field description */
  private WebSecurityContext perfect = mockCtx(new User("perfect",
                                         "Ford Prefect",
                                         "ford.perfect@hitchhiker.com"));

  /** Field description */
  private Repository repository;

  /** Field description */
  private WebSecurityContext slarti = mockCtx(new User("slarti",
                                        "Slartibartfaß",
                                        "slartibartfass@hitchhiker.com"));

  /** Field description */
  private WebSecurityContext marvin = mockCtx(new User("marvin", "Marvin",
                                        "paranoid.android@hitchhiker.com"));

  /** Field description */
  private WebSecurityContext admams = mockCtx(new User("adams",
                                        "Douglas Adams",
                                        "douglas.adams@hitchhiker.com"));
}
