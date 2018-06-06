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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import sonia.scm.ScmState;
import sonia.scm.Type;
import sonia.scm.api.rest.JSONContextResolver;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.repository.Person;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.user.User;
import sonia.scm.util.IOUtil;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class IntegrationTestUtil
{
  
  public static final Person AUTHOR = new Person("SCM Administrator", "scmadmin@scm-manager.org");

  /** Field description */
  public static final String ADMIN_PASSWORD = "scmadmin";

  /** Field description */
  public static final String ADMIN_USERNAME = "scmadmin";
  
  /** scm-manager base url */
  public static final String BASE_URL = "http://localhost:8081/scm/";
  
  /** scm-manager base url for the rest api */
  public static final String REST_BASE_URL = BASE_URL.concat("api/rest/");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private IntegrationTestUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param client
   * @param username
   * @param password
   *
   * @return
   */
  public static ClientResponse authenticate(Client client, String username, String password) {
    WebResource wr =  client.resource(createResourceUrl("auth/access_token"));
    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();

    formData.add("username", username);
    formData.add("password", password);
    formData.add("cookie", "true");
    formData.add("grant_type", "password");

    return wr.type("application/x-www-form-urlencoded").post(ClientResponse.class, formData);
  }

  /**
   * Method description
   *
   *
   * @param client
   *
   * @return
   */
  public static ScmState authenticateAdmin(Client client)
  {
    ClientResponse cr = authenticate(client, ADMIN_USERNAME, ADMIN_PASSWORD);
    ScmState state = cr.getEntity(ScmState.class);

    cr.close();
    assertNotNull(state);
    assertTrue(state.isSuccess());

    User user = state.getUser();

    assertNotNull(user);
    assertEquals("scmadmin", user.getName());
    assertTrue(user.isAdmin());

    Collection<Type> types = state.getRepositoryTypes();

    assertNotNull(types);
    assertFalse(types.isEmpty());

    return state;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static Client createAdminClient()
  {
    Client client = createClient();

    authenticateAdmin(client);

    return client;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static Client createClient()
  {
    DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
    config.getSingletons().add(new JSONContextResolver(new ObjectMapperProvider().get()));
    config.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);

    return ApacheHttpClient.create(config);
  }

  /**
   * Commit and push changes.
   * 
   * @param repositoryClient repository client
   * @param message commit message
   * 
   * @throws IOException 
   * 
   * @since 1.51
   */
  public static void commit(RepositoryClient repositoryClient, String message) throws IOException {
    repositoryClient.getCommitCommand().commit(IntegrationTestUtil.AUTHOR, message);
    if ( repositoryClient.isCommandSupported(ClientCommand.PUSH) ) {
      repositoryClient.getPushCommand().push();
    }
  }
  
  /**
   * Method description
   *
   * @param client
   *
   * @throws IOException
   */
  public static void createRandomFile(RepositoryClient client) throws IOException
  {
    String uuid = UUID.randomUUID().toString();
    String name = "file-" + uuid + ".uuid";

    File file = new File(client.getWorkingCopy(), name);
    try (FileOutputStream out = new FileOutputStream(file)) {
      out.write(uuid.getBytes());
    }

    client.getAddCommand().add(name);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static Collection<String[]> createRepositoryTypeParameters()
  {
    Collection<String[]> params = new ArrayList<>();

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
   * @param client
   * @param url
   *
   * @return
   */
  public static WebResource createResource(Client client, String url)
  {
    return client.resource(createResourceUrl(url));
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   */
  public static String createResourceUrl(String url)
  {
    return REST_BASE_URL.concat(url);
  }
  
  /**
   * Method description
   *
   *
   * @return
   */
  public static File createTempDirectory()
  {
    File directory = new File(System.getProperty("java.io.tmpdir"),
                       UUID.randomUUID().toString());

    IOUtil.mkdirs(directory);

    return directory;
  }

  /**
   * Method description
   *
   *
   * @param client
   */
  public static void logoutClient(Client client)
  {
    WebResource wr = createResource(client, "auth/logout");
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);
    assertEquals(200, response.getStatus());
    response.close();
    client.destroy();
  }
}
