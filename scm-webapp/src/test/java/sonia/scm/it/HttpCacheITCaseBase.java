/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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



package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.Date;

import javax.ws.rs.core.EntityTag;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class HttpCacheITCaseBase<T>
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract T createSampleItem();

  /**
   * Method description
   *
   *
   * @param item
   */
  protected abstract void destroy(T item);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getCollectionUrlPart();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void changingCollectionETagTest()
  {
    ClientResponse response = getCollectionResponse();
    String etag = getETag(response);

    item = createSampleItem();
    response = getCollectionResponse();

    String otherEtag = getETag(response);

    assertThat(etag, not(equalTo(otherEtag)));
  }

  /**
   * Method description
   *
   */
  @Test
  public void changingCollectionLastModifiedTest()
  {
    ClientResponse response = getCollectionResponse();
    long lastModified = getLastModified(response);

    item = createSampleItem();
    response = getCollectionResponse();

    long otherLastModified = getLastModified(response);

    assertEquals(lastModified, otherLastModified);
  }

  /**
   * Method description
   *
   */
  @After
  public void cleanup()
  {
    if (item != null)
    {
      destroy(item);
    }
  }

  /**
   * Method description
   *
   */
  @Test
  public void simpleCollectionETagTest()
  {
    ClientResponse response = getCollectionResponse();
    String etag = getETag(response);

    response = getCollectionResponse();

    String otherEtag = getETag(response);

    assertEquals(etag, otherEtag);
  }

  /**
   * Method description
   *
   */
  @Test
  public void simpleCollectionLastModifiedTest()
  {
    ClientResponse response = getCollectionResponse();
    long lastModified = getLastModified(response);

    response = getCollectionResponse();

    long otherLastModified = getLastModified(response);

    assertEquals(lastModified, otherLastModified);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private ClientResponse getCollectionResponse()
  {
    Client client = createAdminClient();
    WebResource resource = createResource(client, getCollectionUrlPart());
    ClientResponse response = resource.get(ClientResponse.class);

    assertEquals(200, response.getStatus());

    return response;
  }

  /**
   * Method description
   *
   *
   * @param response
   *
   * @return
   */
  private String getETag(ClientResponse response)
  {
    EntityTag e = response.getEntityTag();

    assertNotNull(e);

    String value = e.getValue();

    assertNotNull(value);
    assertTrue(value.length() > 0);

    return value;
  }

  /**
   * Method description
   *
   *
   * @param response
   *
   * @return
   */
  private long getLastModified(ClientResponse response)
  {
    Date lastModified = response.getLastModified();

    assertNotNull(lastModified);

    return lastModified.getTime();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private T item;
}
