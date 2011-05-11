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

import org.junit.Test;

import sonia.scm.client.JerseyClientSession;
import sonia.scm.client.RepositoryClientHandler;
import sonia.scm.client.ScmClientException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyClientRepositoryClientHandlerITCase
        extends AbstractITCaseBase
{

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws ScmClientException
   */
  @Test
  public void testCreate() throws ScmClientException, IOException
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
   * @throws IOException
   * @throws ScmClientException
   */
  @Test(expected = ScmClientException.class)
  public void testCreateAnonymous() throws ScmClientException, IOException
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
   * @throws IOException
   * @throws ScmClientException
   */
  @Test
  public void testDelete() throws ScmClientException, IOException
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
}
