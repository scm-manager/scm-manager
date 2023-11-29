/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.it.webapp;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Resources;
import de.otto.edison.hal.HalRepresentation;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sonia.scm.repository.Person;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

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
  public static final String REST_BASE_URL = BASE_URL.concat("api/v2/");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private IntegrationTestUtil() {}

  //~--- methods --------------------------------------------------------------


  public static ScmClient createAdminClient()
  {
    return new ScmClient("scmadmin", "scmadmin");
  }

  /**
   * Method description
   *
   * @return
   */
  public static Client createClient()
  {
    return ClientBuilder.newBuilder().register(new CustomJacksonMapperProvider()).build();
  }

  public static Entity serialize(Object o, String mediaType) {
    return Entity.entity(o, mediaType);
  }

  public static Collection<String[]> createRepositoryTypeParameters() {
    Collection<String[]> params = new ArrayList<>();

    params.add(new String[]{"git"});
    params.add(new String[]{"svn"});

    if (IOUtil.search("hg") != null)
    {
      params.add(new String[]{"hg"});
    }

    return params;
  }

  public static URI getLink(HalRepresentation object, String linkName) {
    return URI.create(object.getLinks().getLinkBy("delete").get().getHref());
  }

  public static Invocation.Builder createResource(ScmClient client, String url) {
    return createResource(client, createResourceUrl(url));
  }
  public static Invocation.Builder createResource(ScmClient client, String url, String mediaType) {
    return createResource(client, createResourceUrl(url), mediaType);
  }
  public static Invocation.Builder createResource(ScmClient client, URI url) {
    return client.resource(url.toString());
  }

  public static Invocation.Builder createResource(ScmClient client, URI url, String mediaType) {
    return client.resource(url.toString(), MediaType.valueOf(mediaType));
  }

  public static Response post(ScmClient client, String path, String mediaType, Object o) {
    return createResource(client, path, mediaType)
      .post(serialize(o, mediaType));
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   */
  public static URI createResourceUrl(String url)
  {
    return URI.create(REST_BASE_URL).resolve(url);
  }

  public static String readJson(String jsonFileName) {
    URL url = Resources.getResource(jsonFileName);
    try {
      return Resources.toString(url, Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw new RuntimeException("could not read json file " + jsonFileName, e);
    }
  }
}
