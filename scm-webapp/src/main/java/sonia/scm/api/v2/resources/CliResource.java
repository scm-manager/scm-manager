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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import sonia.scm.cli.CliProcessor;
import sonia.scm.cli.JsonStreamingCliContext;
import sonia.scm.security.ApiKeyService;

import java.util.List;

import static sonia.scm.cli.UserAgentClientParser.parse;

@OpenAPIDefinition(tags = {
  @Tag(name = "CLI", description = "Command-line interface related endpoints")
})
@Path("v2/cli")
public class CliResource {

  private final CliProcessor processor;
  private final ApiKeyService service;

  @Inject
  public CliResource(CliProcessor processor, ApiKeyService service) {
    this.processor = processor;
    this.service = service;
  }

  @POST
  @Path("exec")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(summary = "Execute", description = "Execute commands", tags = "CLI")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_OCTET_STREAM
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  public StreamingOutput exec(@QueryParam("args") List<String> args, @Context HttpServletRequest request) {
    return outputStream -> {
      try (JsonStreamingCliContext context = new JsonStreamingCliContext(
        request.getLocale(),
        parse(request.getHeader("User-Agent")),
        request.getInputStream(),
        outputStream
      )) {
        int exitCode = processor.execute(context, args.toArray(new String[0]));
        context.writeExit(exitCode);
      }
    };
  }

  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Log in", description = "Create api key to connect to the SCM-Manager server", tags = "CLI")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "404", description = "API keys disabled globally")
  @ApiResponse(responseCode = "409", description = "There already is an API key with this name")
  @ApiResponse(responseCode = "500", description = "internal server error")
  public String login(CliAuthenticationDto auth) {
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    ApiKeyService.CreationResult newKey = service.createNewKey(username, auth.getApiKey(), "*");
    return newKey.getToken();
  }

  @DELETE
  @Path("logout/{apiKey}")
  @Operation(summary = "Log out", description = "Remove api key from SCM-Manager server", tags = "CLI")
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "400", description = "bad request, required parameter is missing")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  public void logout(@PathParam("apiKey") String apiKeyName) {
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    service.getKeys(username)
      .stream()
      .filter(apiKey -> apiKey.getDisplayName().equals(apiKeyName))
      .findFirst()
      .ifPresent(apiKey -> service.remove(username, apiKey.getId()));
  }

  @Data
  static class CliAuthenticationDto {
    private String apiKey;
  }
}
