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

import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.TemplateCommand;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import javax.inject.Provider;

@CommandLine.Command(name = "create")
@ParentCommand(value = RepositoryCommand.class)
public class RepositoryCreateCommand extends TemplateCommand implements Runnable {

  private final RepositoryManager manager;
  private final RepositoryInitializer repositoryInitializer;
  private final Provider<NamespaceStrategy> namespaceStrategyProvider;

  @CommandLine.Parameters
  private String type;
  @CommandLine.Parameters
  private String repository;
  @CommandLine.Option(names = "--description")
  private String description;
  @CommandLine.Option(names = "--contact")
  private String contact;
  @CommandLine.Option(names = "--init")
  private boolean init;


  @Inject
  public RepositoryCreateCommand(RepositoryManager manager,
                                 CliContext context,
                                 TemplateEngineFactory templateEngineFactory,
                                 RepositoryInitializer repositoryInitializer,
                                 Provider<NamespaceStrategy> namespaceStrategyProvider) {
    super(context, templateEngineFactory);
    this.manager = manager;
    this.repositoryInitializer = repositoryInitializer;
    this.namespaceStrategyProvider = namespaceStrategyProvider;
  }

  @Override
  public void run() {
    Repository newRepo = new Repository();
    String[] splitRepoName = repository.split("/");
    if (splitRepoName.length == 2) {
      newRepo.setNamespace(splitRepoName[0]);
      newRepo.setName(splitRepoName[1]);
      namespaceStrategyProvider.get().createNamespace(newRepo);
    } else {
      newRepo.setName(repository);
    }
    newRepo.setType(type);
    newRepo.setDescription(description);
    newRepo.setContact(contact);
    try {
      Repository createdRepo = manager.create(newRepo);
      if (init) {
        repositoryInitializer.initialize(createdRepo, ImmutableMap.of());
      }
      template(RepositoryGetCommand.DEFAULT_TEMPLATE, ImmutableMap.of("repo", createdRepo));
    }
    catch (Exception e) {
      errorTemplate(DEFAULT_ERROR_TEMPLATE, ImmutableMap.of("error", e.getMessage()));
    }
  }
}
