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

package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.shiro.authz.AuthorizationException;
import sonia.scm.lifecycle.AdminAccountStartupAction;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AllowAnonymousAccess;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@AllowAnonymousAccess
@Extension
public class AdminAccountStartupResource implements InitializationStepResource {

  private final AdminAccountStartupAction adminAccountStartupAction;

  @Inject
  public AdminAccountStartupResource(AdminAccountStartupAction adminAccountStartupAction) {
    this.adminAccountStartupAction = adminAccountStartupAction;
  }

  @POST
  @Path("")
  @Consumes("application/json")
  public void post(JsonNode data) {
    doThrow()
      .violation("initialization not necessary")
      .when(adminAccountStartupAction.done());

    String givenInitialPassword = data.get("initialPassword").asText();

    if (!adminAccountStartupAction.isCorrectToken(givenInitialPassword)) {
      throw new AuthorizationException("wrong password");
    }

    String userName = data.get("userName").asText();
    String password = data.get("password").asText();
    String passwordConfirmation = data.get("passwordConfirmation").asText();

    doThrow()
      .violation("password and confirmation differ", "password")
      .when(!password.equals(passwordConfirmation));

    adminAccountStartupAction.createAdminUser(userName, password);
  }

  public String name() {
    return adminAccountStartupAction.name();
  }
}
