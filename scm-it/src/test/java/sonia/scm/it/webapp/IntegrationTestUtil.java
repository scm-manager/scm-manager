/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.it.webapp;


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


public final class IntegrationTestUtil
{

  public static final Person AUTHOR = new Person("SCM Administrator", "scmadmin@scm-manager.org");

  public static final String ADMIN_PASSWORD = "scmadmin";

  public static final String ADMIN_USERNAME = "scmadmin";

  /** scm-manager base url */
  public static final String BASE_URL = "http://localhost:8081/scm/";

  /** scm-manager base url for the rest api */
  public static final String REST_BASE_URL = BASE_URL.concat("api/v2/");


  private IntegrationTestUtil() {}



  public static ScmClient createAdminClient()
  {
    return new ScmClient("scmadmin", "scmadmin");
  }


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
