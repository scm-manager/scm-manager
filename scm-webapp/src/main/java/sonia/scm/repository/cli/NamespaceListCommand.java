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

package sonia.scm.repository.cli;

import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.RepositoryManager;

import static java.util.Collections.emptyMap;

@ParentCommand(value = NamespaceCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
class NamespaceListCommand implements Runnable {


  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final RepositoryManager manager;

  @Inject
  public NamespaceListCommand(RepositoryManager manager, TemplateRenderer templateRenderer, RepositoryToRepositoryCommandDtoMapper mapper) {
    this.manager = manager;
    this.templateRenderer = templateRenderer;
  }

  @Override
  public void run() {
    manager.getAllNamespaces()
      .forEach(this::render);
  }

  private void render(String namespace) {
    templateRenderer.renderToStdout(namespace + "\n", emptyMap());
  }
}
