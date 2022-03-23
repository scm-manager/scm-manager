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

import com.cronutils.utils.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;

@CommandLine.Command(name = "get")
public class RepositoryGetCommand extends TemplateCommand implements Runnable {

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0")
  private String repository;

  private static final String DEFAULT_TEMPLATE = String.join("\n",
    "{{repo.namespace}}/{{repo.name}}",
    "Description: {{repo.description}}",
    "Type: {{repo.type}}",
    "Contact: {{repo.contact}}"
  );

  private static final String NOT_FOUND_TEMPLATE = "Could not find repository {{repository}}";
  private static final String INVALID_TEMPLATE = "Invalid input {{repository}}. Use {namespace/name}.";

  private final RepositoryManager manager;

  @Inject
  RepositoryGetCommand(CliContext context, TemplateEngineFactory templateEngineFactory, RepositoryManager manager) {
    super(context, templateEngineFactory);
    this.manager = manager;
  }

  @VisibleForTesting
  public void setRepository(String repository) {
    this.repository = repository;
  }

  @Override
  public void run() {
    String[] splitRepo = repository.split("/");
    if (splitRepo.length != 2) {
      errorTemplate(INVALID_TEMPLATE, ImmutableMap.of("repo", repository));
      context.exit(2);
    }
    Repository repo = manager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));
    if (repo != null) {
      template(DEFAULT_TEMPLATE, ImmutableMap.of("repo", repo));
    } else {
      errorTemplate(NOT_FOUND_TEMPLATE, ImmutableMap.of("repo", repository));
      context.exit(1);
    }
  }
}
