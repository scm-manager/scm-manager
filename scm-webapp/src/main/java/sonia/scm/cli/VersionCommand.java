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

package sonia.scm.cli;

import com.google.common.collect.ImmutableMap;
import lombok.Value;
import picocli.CommandLine;
import sonia.scm.SCMContextProvider;

import javax.inject.Inject;

@CommandLine.Command(name = "version")
public class VersionCommand implements Runnable{

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
