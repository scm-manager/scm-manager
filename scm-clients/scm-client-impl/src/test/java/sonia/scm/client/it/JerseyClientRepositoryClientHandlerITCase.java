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



package sonia.scm.client.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.AfterClass;
import org.junit.Test;

import sonia.scm.client.JerseyClientSession;
import sonia.scm.client.RepositoryClientHandler;
import sonia.scm.client.ScmForbiddenException;
import sonia.scm.client.ScmUnauthorizedException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.util.Util;

import static org.junit.Assert.*;

import static sonia.scm.client.it.TestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyClientRepositoryClientHandlerITCase
{

  /**
   * Method description
   *
   *
   */
  @AfterClass
  public static void removeTestRepositories()
  {
    JerseyClientSession session = createAdminSession();
    RepositoryClientHandler handler = session.getRepositoryHandler();
    List<Repository> repositories = handler.getAll();

    if (Util.isNotEmpty(repositories))
    {
      for (Repository r : repositories)
      {
        handler.delete(r);
      }
    }

    session.close();
    setAnonymousAccess(false);
  }

  /**
   * Method description
   *
   *
   */
  @Test
  public void testCreate()
  {
    JerseyClientSession session = createAdminSession();
    Repository hog = RepositoryTestData.createHeartOfGold(REPOSITORY_TYPE);
    RepositoryClientHandler handler = session.getRepositoryHandler();

    handler.create(hog);
    assertNotNull(hog.getId());
    assertNotNull(hog.getCreationDate());

    String id = hog.getId();
    Repository r = handler.get(id);

    assertNotNull(r);
    assertEquals(hog, r);
    session.close();
  }

  /**
   * Method description
   *
   *
   */
  @Test
  public void testDelete()
  {
    JerseyClientSession session = createAdminSession();
    Repository hvpt =
      RepositoryTestData.createHappyVerticalPeopleTransporter(REPOSITORY_TYPE);
    RepositoryClientHandler handler = session.getRepositoryHandler();

    handler.create(hvpt);
    assertNotNull(hvpt.getId());
    assertNotNull(hvpt.getCreationDate());

    String id = hvpt.getId();

    handler.delete(hvpt);

    Repository r = handler.get(id);

    assertNull(r);
  }

  /**
   * Method description
   *
   *
   */
  @Test(expected = ScmUnauthorizedException.class)
  public void testDisabledCreateAnonymous()
  {
    JerseyClientSession session = createAnonymousSession();
    Repository p42 = RepositoryTestData.create42Puzzle(REPOSITORY_TYPE);

    session.getRepositoryHandler().create(p42);
    session.close();
  }

  /**
   * Method description
   *
   *
   */
  @Test
  public void testEnabledCreateAnonymous()
  {
    setAnonymousAccess(true);

    JerseyClientSession session = createAnonymousSession();
    Repository p42 = RepositoryTestData.create42Puzzle(REPOSITORY_TYPE);
    boolean forbidden = false;

    try
    {
      session.getRepositoryHandler().create(p42);
    }
    catch (ScmForbiddenException ex)
    {
      forbidden = true;
    }

    assertTrue(forbidden);
    session.close();
    setAnonymousAccess(false);
  }
}
