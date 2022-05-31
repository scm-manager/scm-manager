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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import sonia.scm.initialization.InitializationAuthenticationService;
import sonia.scm.initialization.InitializationStepResource;
import sonia.scm.lifecycle.AdminAccountStartupAction;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.security.Tokens;
import sonia.scm.util.ValidationUtil;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static de.otto.edison.hal.Link.link;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;
import static sonia.scm.initialization.InitializationWebTokenGenerator.INIT_TOKEN_HEADER;

@AllowAnonymousAccess
@Extension
public class AdminAccountStartupResource implements InitializationStepResource {

  private final AdminAccountStartupAction adminAccountStartupAction;
  private final ResourceLinks resourceLinks;
  private final InitializationAuthenticationService authenticationService;

  @Inject
  public AdminAccountStartupResource(AdminAccountStartupAction adminAccountStartupAction, ResourceLinks resourceLinks, InitializationAuthenticationService authenticationService) {
    this.adminAccountStartupAction = adminAccountStartupAction;
    this.resourceLinks = resourceLinks;
    this.authenticationService = authenticationService;
  }

  @POST
  @Path("")
  @Consumes("application/json")
  public Response postAdminInitializationData(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @Valid AdminInitializationData data
  ) {
    verifyInInitialization();
    verifyToken(data);
    createAdminUser(data);

    // Invalidate old access token cookies to prevent conflicts during authentication
    authenticationService.invalidateCookies(request, response);

    SecurityUtils.getSubject().login(Tokens.createAuthenticationToken(request, data.userName, data.password));
    // Create cookie which will be used for authentication during the initialization process
    authenticationService.authenticate(request, response);
    return Response.noContent().build();
  }

  private void verifyInInitialization() {
    doThrow()
      .violation("initialization not necessary")
      .when(adminAccountStartupAction.done());
  }

  private void verifyToken(AdminInitializationData data) {
    String givenStartupToken = data.getStartupToken();

    if (!adminAccountStartupAction.isCorrectToken(givenStartupToken)) {
      throw new UnauthenticatedException("wrong password");
    }
  }

  private void createAdminUser(AdminInitializationData data) {
    String userName = data.getUserName();
    String displayName = data.getDisplayName();
    String email = data.getEmail();
    String password = data.getPassword();
    String passwordConfirmation = data.getPasswordConfirmation();

    verifyPasswordConfirmation(password, passwordConfirmation);

    adminAccountStartupAction.createAdminUser(userName, displayName, email, password);
  }

  private void verifyPasswordConfirmation(String password, String passwordConfirmation) {
    doThrow()
      .violation("password and confirmation differ", "password")
      .when(!password.equals(passwordConfirmation));
  }

  @Override
  public void setupIndex(Links.Builder builder, Embedded.Builder embeddedBuilder) {
    String link = resourceLinks.initialAdminAccount().indexLink(name());
    builder.single(link("initialAdminUser", link));
  }

  @Override
  public String name() {
    return adminAccountStartupAction.name();
  }

  @Data
  static class AdminInitializationData {
    @NotEmpty
    private String startupToken;
    @Pattern(regexp = ValidationUtil.REGEX_NAME)
    private String userName;
    @NotEmpty
    private String displayName;
    @Email
    private String email;
    @NotEmpty
    private String password;
    @NotEmpty
    private String passwordConfirmation;
  }
}
