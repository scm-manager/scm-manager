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



package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.client.RepositoryClient;
import sonia.scm.repository.client.RepositoryClientException;
import sonia.scm.repository.client.RepositoryClientFactory;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;
import static sonia.scm.it.RepositoryITUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class ChangesetViewerITCase extends AbstractAdminITCaseBase
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryType
   */
  public ChangesetViewerITCase(String repositoryType)
  {
    this.repositoryType = repositoryType;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Parameters
  public static Collection<String[]> createParameters()
  {
    Collection<String[]> params = new ArrayList<String[]>();

    params.add(new String[] { "git" });
    params.add(new String[] { "svn" });

    if (IOUtil.search("hg") != null)
    {
      params.add(new String[] { "hg" });
    }

    return params;
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @After
  public void cleanup() throws IOException
  {
    IOUtil.delete(localDirectory, true);
    deleteRepository(client, repository.getId());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Before
  public void setup() throws IOException
  {
    repository = RepositoryTestData.createHeartOfGold(repositoryType);
    repository = createRepository(client, repository);
    localDirectory = File.createTempFile("scm-", ".unittest");
    IOUtil.delete(localDirectory);
    IOUtil.mkdirs(localDirectory);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test
  public void testChagensetViewer()
          throws RepositoryClientException, IOException
  {
    String checkoutUrl = repository.getUrl();
    RepositoryClient rc = RepositoryClientFactory.createClient(repositoryType,
                            localDirectory, checkoutUrl,
                            IntegrationTestUtil.ADMIN_USERNAME,
                            IntegrationTestUtil.ADMIN_PASSWORD);

    rc.checkout();

    File file = new File(localDirectory, "a.txt");

    writeRandomContent(file);
    rc.add("a.txt");
    rc.commit("added-a.txt");

    ChangesetPagingResult cpr = getChangesets(repository);

    assertTrue((cpr.getTotal() > 0) && (cpr.getTotal() <= 2));    // hg/git == 1 & svn == 2

    List<Changeset> changesets = cpr.getChangesets();

    assertNotNull(changesets);
    assertTrue((changesets.size() > 0) && (changesets.size() <= 2));

    Changeset c = changesets.get(0);

    assertNotNull(c);
    assertEquals("added-a.txt", c.getDescription());
    assertTrue(c.isValid());

    Modifications m = c.getModifications();

    assertNotNull(m);

    List<String> added = m.getAdded();

    assertNotNull(added);
    assertFalse(added.isEmpty());
    assertTrue(added.size() == 1);
    assertTrue("a.txt".equals(added.get(0)) || "/a.txt".equals(added.get(0)));
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @throws IOException
   */
  private void writeRandomContent(File file) throws IOException
  {
    FileOutputStream output = null;

    try
    {
      output = new FileOutputStream(file);

      Random random = new Random();
      byte[] data = new byte[random.nextInt(1024)];

      random.nextBytes(data);
      output.write(data);
    }
    finally
    {
      IOUtil.close(output);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private String getChangesetViewerUri(Repository repository)
  {
    return "repositories/".concat(repository.getId()).concat("/changesets");
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private ChangesetPagingResult getChangesets(Repository repository)
  {
    WebResource resource = createResource(client,
                             getChangesetViewerUri(repository));

    assertNotNull(resource);

    ClientResponse response = resource.get(ClientResponse.class);

    assertNotNull(response);
    assertTrue(response.getStatus() == 200);

    ChangesetPagingResult cpr = response.getEntity(ChangesetPagingResult.class);

    assertNotNull(cpr);

    return cpr;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File localDirectory;

  /** Field description */
  private Repository repository;

  /** Field description */
  private String repositoryType;
}
