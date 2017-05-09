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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.api.rest.RestActionResult;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import sonia.scm.security.Role;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("action/change-password")
public class ChangePasswordResource
{

  /** the logger for ChangePasswordResource */
  private static final Logger logger =
    LoggerFactory.getLogger(ChangePasswordResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param userManager
   * @param encryptionHandler
   * @param securityContextProvider
   */
  @Inject
  public ChangePasswordResource(UserManager userManager,
    EncryptionHandler encryptionHandler)
  {
    this.userManager = userManager;
    this.encryptionHandler = encryptionHandler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Changes the password of the current user.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 success</li>
   *   <li>400 bad request, the old password is not correct</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param oldPassword old password of the current user
   * @param newPassword new password for the current user
   *
   * @return
   *
   * @throws IOException
   * @throws UserException
   */
  @POST
  @TypeHint(RestActionResult.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response changePassword(@FormParam("old-password") String oldPassword,
    @FormParam("new-password") String newPassword)
    throws UserException, IOException
  {
    AssertUtil.assertIsNotEmpty(oldPassword);
    AssertUtil.assertIsNotEmpty(newPassword);

    int length = newPassword.length();

    if ((length < 6) || (length > 32))
    {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Response response = null;
    Subject subject = SecurityUtils.getSubject();

    if (!subject.hasRole(Role.USER))
    {
      throw new ScmSecurityException("user is not authenticated");
    }

    User currentUser = subject.getPrincipals().oneByType(User.class);

    if (logger.isInfoEnabled())
    {
      logger.info("password change for user {}", currentUser.getName());
    }

    // Only account of the default type can change their password
    if (currentUser.getType().equals(userManager.getDefaultType()))
    {
      User dbUser = userManager.get(currentUser.getName());

      if (encryptionHandler.encrypt(oldPassword).equals(dbUser.getPassword()))
      {
        dbUser.setPassword(encryptionHandler.encrypt(newPassword));
        userManager.modify(dbUser);
        response = Response.ok(new RestActionResult(true)).build();
      }
      else
      {
        response = Response.status(Response.Status.BAD_REQUEST).build();
      }
    }
    else
    {
      //J-
      logger.error(
        "Only account of the default type ({}) can change their password",
        userManager.getDefaultType()
      );
      //J+
      response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return response;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private EncryptionHandler encryptionHandler;

  /** Field description */
  private UserManager userManager;
}
