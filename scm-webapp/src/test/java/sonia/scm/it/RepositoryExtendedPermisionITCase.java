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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.client.RepositoryClient;
import sonia.scm.repository.client.RepositoryClientException;
import sonia.scm.repository.client.RepositoryClientFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.IOUtil;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.UUID;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryExtendedPermisionITCase
{

  /** Field description */
  private static Repository REPOSITORY = null;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @AfterClass
  public static void cleanup()
  {
    Client client = createAdminClient();

    createResource(client, "users/trillian").delete();
    createResource(client, "repositories/" + REPOSITORY.getId()).delete();
    client.destroy();
  }

  /**
   * Method description
   *
   */
  @BeforeClass
  public static void setup()
  {
    Client client = createAdminClient();
    User trillian = UserTestData.createTrillian();

    trillian.setPassword("secret");
    createResource(client, "users").post(trillian);

    Repository repository = RepositoryTestData.createHeartOfGold("git");

    repository.setPermissions(Arrays.asList(new Permission("trillian",
            PermissionType.WRITE)));

    ClientResponse response = createResource(client,
                                "repositories").post(ClientResponse.class,
                                  repository);
    String url = response.getHeaders().get("Location").get(0) + EXTENSION;

    response.close();
    REPOSITORY = client.resource(url).get(Repository.class);
    client.destroy();
  }

  /**
   * Method description
   *
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test
  public void write() throws RepositoryClientException, IOException
  {
    File directory = new File(System.getProperty("java.io.tmpdir"),
                              UUID.randomUUID().toString());

    try
    {
      RepositoryClient rc = RepositoryClientFactory.createClient("git",
                              directory, REPOSITORY.getUrl(), "trillian",
                              "secret");

      rc.init();

      for (int i = 0; i < 5; i++)
      {
        createRandomFile(rc, directory, i);
      }

      rc.commit("added some test files");
    }
    finally
    {
      IOUtil.delete(directory);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param client
   * @param directory
   * @param i
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  private void createRandomFile(RepositoryClient client, File directory, int i)
          throws IOException, RepositoryClientException
  {
    String name = "file-" + i + ".uuid";
    FileOutputStream out = null;

    try
    {
      out = new FileOutputStream(new File(directory, name));
      out.write(UUID.randomUUID().toString().getBytes());
    }
    finally
    {
      IOUtil.close(out);
    }

    client.add(name);
  }
}
