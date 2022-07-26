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
import sonia.scm.repository.RepositoryPermissionHolder;

import java.util.Collection;
import java.util.Optional;

abstract class PermissionsListCommand<T extends RepositoryPermissionHolder> implements Runnable {

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final PermissionBaseAdapter<T> adapter;
  private final RepositoryPermissionBeanMapper beanMapper;

  @CommandLine.Option(names = {"--verbose", "-v"}, descriptionKey = "scm.repo.list-permissions.verbose")
  private boolean verbose;
  @CommandLine.Option(names = {"--keys", "-k"}, descriptionKey = "scm.repo.list-permissions.keys")
  private boolean keys;

  PermissionsListCommand(RepositoryTemplateRenderer templateRenderer, CommandValidator validator, PermissionBaseAdapter<T> adapter, RepositoryPermissionBeanMapper beanMapper) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.adapter = adapter;
    this.beanMapper = beanMapper;
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
    Optional<T> object = adapter.get(getIdentifier());
    if (object.isPresent()) {
      Collection<RepositoryPermissionBean> permissions = beanMapper.createPermissionBeans(object.get().getPermissions(), keys);
      if (verbose) {
        templateRenderer.renderVerbose(permissions);
      } else {
        templateRenderer.render(permissions);
      }
    }
  }

  abstract String getIdentifier();
}
