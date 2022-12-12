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

import com.sun.jersey.api.client.ClientResponse;
import de.otto.edison.hal.HalRepresentation;
import org.junit.Assert;
import org.junit.Assume;
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
                         "scm@example.com");

    user.setPassword("hallo123");
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
    Assume.assumeTrue(credentials.getUsername() == null);
    if (!credentials.isAnonymous())
    {
      assertNotNull(response);
      Assert.assertEquals(200, response.getStatus());

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
