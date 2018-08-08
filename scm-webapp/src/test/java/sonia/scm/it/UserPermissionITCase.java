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

import com.sun.jersey.api.client.ClientResponse;
import de.otto.edison.hal.HalRepresentation;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class UserPermissionITCase extends AbstractPermissionITCaseBase<User>
{

  /**
   * Constructs ...
   *
   *
   * @param credentials
   */
  public UserPermissionITCase(Credentials credentials, String ignore_testCaseName)
  {
    super(credentials);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getBasePath()
  {
    return "users";
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected User getCreateItem()
  {
    return UserTestData.createZaphod();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getDeletePath()
  {
    return "users/scmadmin";
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getGetPath()
  {
    return "users/scmadmin";
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected User getModifyItem()
  {
    User user = new User("scmadmin", "SCM Administrator",
                         "scm-admin@scm-manager.org");

    user.setPassword("hallo123");
    user.setAdmin(true);
    user.setType("xml");

    return user;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getModifyPath()
  {
    return "users/scmadmin";
  }

  @Override
  protected String getMediaType() {
    return VndMediaType.USER;
  }

  @Override
  protected void checkGetAllResponse(ClientResponse response)
  {
    if (!credentials.isAnonymous())
    {
      assertNotNull(response);
      assertEquals(200, response.getStatus());

      HalRepresentation repositories =
        null;
      try {
        repositories = new ObjectMapperProvider().get().readValue(response.getEntity(String.class), HalRepresentation.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      assertNotNull(repositories);
      assertTrue(repositories.getEmbedded().getItemsBy("users").isEmpty());
      response.close();
    }
  }
}
