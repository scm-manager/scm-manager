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

import lombok.Data;
import org.apache.shiro.SecurityUtils;
import sonia.scm.cli.CliProcessor;
import sonia.scm.cli.JsonStreamingCliContext;
import sonia.scm.security.ApiKeyService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.List;

@Path("v2/cli")
public class CliResource {

  private static final String API_KEY_PREFIX = "cli-";

  private final CliProcessor processor;
  private final ApiKeyService service;

  @Inject
  public CliResource(CliProcessor processor, ApiKeyService service) {
    this.processor = processor;
    this.service = service;
  }

  @POST
  @Path("exec")
  public StreamingOutput exec(@QueryParam("args") List<String> args, @Context HttpServletRequest request) {
    return outputStream -> {
      try (JsonStreamingCliContext context = new JsonStreamingCliContext(request.getLocale(), request.getInputStream(), outputStream)) {
        processor.execute(context, args.toArray(new String[0]));
      }
    };
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("login")
  public Response login(CliAuthenticationDto auth) {
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    ApiKeyService.CreationResult newKey = service.createNewKey(username, API_KEY_PREFIX + auth.getHostname(), "*");
    return Response.ok(newKey.getToken()).build();
  }

  @DELETE
  @Path("logout/{hostname}")
  public Response logout(@PathParam("hostname") String hostname) {
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    service.getKeys(username)
      .stream()
      .filter(apiKey -> apiKey.getDisplayName().equals(API_KEY_PREFIX + hostname))
      .findFirst()
      .ifPresent(apiKey -> service.remove(username, apiKey.getId()));
    return Response.noContent().build();
  }

  @Data
  static class CliAuthenticationDto {
    private String hostname;
  }
}
