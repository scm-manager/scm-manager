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


import de.otto.edison.hal.HalRepresentation;
import jakarta.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.web.VndMediaType;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(Parameterized.class)
public class UserPermissionITCase extends AbstractPermissionITCaseBase<User>
{

 
  public UserPermissionITCase(Credentials credentials, String ignore_testCaseName)
  {
    super(credentials);
  }


  
  @Override
  protected String getBasePath()
  {
    return "users";
  }

  
  @Override
  protected User getCreateItem()
  {
    return UserTestData.createZaphod();
  }

  
  @Override
  protected String getDeletePath()
  {
    return "users/scmadmin";
  }

  
  @Override
  protected String getGetPath()
  {
    return "users/scmadmin";
  }

  
  @Override
  protected User getModifyItem()
  {
    User user = new User("scmadmin", "SCM Administrator",
                         "scm@example.com");

    user.setPassword("hallo123");
    user.setType("xml");

    return user;
  }

  
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
  protected void checkGetAllResponse(Response response)
  {
    Assume.assumeTrue(credentials.getUsername() == null);
    if (!credentials.isAnonymous())
    {
      assertNotNull(response);
      Assert.assertEquals(200, response.getStatus());

      HalRepresentation repositories = (HalRepresentation) response.getEntity();

      assertNotNull(repositories);
      assertTrue(repositories.getEmbedded().getItemsBy("users").isEmpty());
      response.close();
    }
  }
}
