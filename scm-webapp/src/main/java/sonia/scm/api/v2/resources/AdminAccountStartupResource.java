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

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
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

import static de.otto.edison.hal.Link.link;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

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
