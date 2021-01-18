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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import de.otto.edison.hal.HalRepresentation;
import sonia.scm.api.rest.JSONContextResolver;
import sonia.scm.api.rest.ObjectMapperProvider;
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

  public static String serialize(Object o) {
    ObjectMapper mapper = new ObjectMapperProvider().get();
    try {
      return mapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
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

  public static WebResource.Builder createResource(ScmClient client, String url) {
    return createResource(client, createResourceUrl(url));
  }
  public static WebResource.Builder createResource(ScmClient client, URI url) {
    return client.resource(url.toString());
  }

  public static ClientResponse post(ScmClient client, String path, String mediaType, Object o) {
    return createResource(client, path)
      .type(mediaType)
      .post(ClientResponse.class, serialize(o));
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
