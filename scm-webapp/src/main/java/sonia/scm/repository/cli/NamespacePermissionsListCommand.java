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

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;

@CommandLine.Command(name = "list-permissions")
@ParentCommand(value = NamespaceCommand.class)
class NamespacePermissionsListCommand implements Runnable {

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final NamespaceManager manager;
  private final RepositoryPermissionBeanMapper beanMapper;

  @CommandLine.Parameters(paramLabel = "namespace", index = "0", descriptionKey = "scm.namespace.list-permissions.namespace")
  private String namespace;
  @CommandLine.Option(names = {"--verbose", "-v"}, descriptionKey = "scm.namespace.list-permissions.verbose")
  private boolean verbose;
  @CommandLine.Option(names = {"--keys", "-k"}, descriptionKey = "scm.namespace.list-permissions.keys")
  private boolean keys;

  @Inject
  public NamespacePermissionsListCommand(RepositoryTemplateRenderer templateRenderer, CommandValidator validator, NamespaceManager manager, RepositoryPermissionBeanMapper beanMapper) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.manager = manager;
    this.beanMapper = beanMapper;
  }

  @VisibleForTesting
  void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @VisibleForTesting
  void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void setKeys(boolean keys) {
    this.keys = keys;
  }

  @Override
  public void run() {
    validator.validate();
    Optional<Namespace> ns = manager.get(namespace);

    if (ns.isPresent()) {
      Collection<RepositoryPermissionBean> permissions = beanMapper.createPermissionBeans(ns.get().getPermissions(), keys);
      if (verbose) {
        templateRenderer.renderVerbose(permissions);
      } else {
        templateRenderer.render(permissions);
      }
    } else {
      templateRenderer.renderNotFoundError();
    }
  }
}
