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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
