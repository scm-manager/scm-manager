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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.initialization.InitializationStepResource;
import sonia.scm.lifecycle.PluginWizardStartupAction;
import sonia.scm.lifecycle.PrivilegedStartupAction;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginSet;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.web.VndMediaType;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static de.otto.edison.hal.Link.link;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Extension
public class PluginWizardStartupResource implements InitializationStepResource {

  private final PluginWizardStartupAction pluginWizardStartupAction;
  private final ResourceLinks resourceLinks;
  private final PluginManager pluginManager;

  private final AccessTokenCookieIssuer cookieIssuer;

  private final PluginSetDtoMapper pluginSetDtoMapper;

  private final AdministrationContext context;


  @Inject
  public PluginWizardStartupResource(PluginWizardStartupAction pluginWizardStartupAction, ResourceLinks resourceLinks, PluginManager pluginManager, AccessTokenCookieIssuer cookieIssuer, PluginSetDtoMapper pluginSetDtoMapper, AdministrationContext context) {
    this.pluginWizardStartupAction = pluginWizardStartupAction;
    this.resourceLinks = resourceLinks;
    this.pluginManager = pluginManager;
    this.cookieIssuer = cookieIssuer;
    this.pluginSetDtoMapper = pluginSetDtoMapper;
    this.context = context;
  }

  @Override
  public void setupIndex(Links.Builder builder, Embedded.Builder embeddedBuilder) {
    setupIndex(builder, embeddedBuilder, Locale.getDefault());
  }

  @Override
  public void setupIndex(Links.Builder builder, Embedded.Builder embeddedBuilder, Locale locale) {
    context.runAsAdmin((PrivilegedStartupAction)() -> {
      Set<PluginSet> pluginSets = pluginManager.getPluginSets();
      List<AvailablePlugin> availablePlugins = pluginManager.getAvailable();
      List<PluginSetDto> pluginSetDtos = pluginSetDtoMapper.map(pluginSets, availablePlugins, locale);
      embeddedBuilder.with("pluginSets", pluginSetDtos);
      String link = resourceLinks.pluginWizard().indexLink(name());
      builder.single(link("installPluginSets", link));
    });
  }

  @Override
  public String name() {
    return pluginWizardStartupAction.name();
  }

  @POST
  @Path("")
  @Consumes("application/json")
  @Operation(
    summary = "Install plugin sets and restart",
    description = "Installs all plugins contained in the provided plugin sets and restarts the server",
    tags = "Plugin Management",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = MediaType.APPLICATION_JSON,
        schema = @Schema(implementation = PluginSetsInstallDto.class)
      )
    )
  )
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response installPluginSets(@Context HttpServletRequest request,
                                    @Context HttpServletResponse response,
                                    @Valid PluginSetsInstallDto dto) {
    verifyInInitialization();
    cookieIssuer.invalidate(request, response);

    pluginManager.installPluginSets(dto.getPluginSetIds(), true);

    return Response.ok().build();
  }

  private void verifyInInitialization() {
    doThrow()
      .violation("initialization not necessary")
      .when(pluginWizardStartupAction.done());
  }
}
