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

package sonia.scm.cli;

import com.google.common.collect.ImmutableMap;
import jakarta.inject.Inject;
import lombok.Value;
import picocli.CommandLine;
import sonia.scm.SCMContextProvider;

@CommandLine.Command(name = "version")
class VersionCommand implements Runnable{

  private static final String TEMPLATE = String.join("\n","Client Version: {{client.version}}", "Server Version: {{server.version}}");

  private final CliContext context;
  private final SCMContextProvider scmContextProvider;
  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;

  @Inject
  public VersionCommand(CliContext context, SCMContextProvider scmContextProvider, TemplateRenderer templateRenderer) {
    this.context = context;
    this.scmContextProvider = scmContextProvider;
    this.templateRenderer = templateRenderer;
  }

  @Override
  public void run() {
    templateRenderer.renderToStdout(
      TEMPLATE,
      createModel()
    );
  }

  private ImmutableMap<String, Object> createModel() {
    return ImmutableMap.of("client", context.getClient(), "server", new Server(scmContextProvider.getVersion()));
  }

  @Value
  static class Server {
    String version;
  }
}
