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
