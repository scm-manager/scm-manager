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

package sonia.scm.group.cli;

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.repository.cli.GroupCommand;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

@ParentCommand(GroupCommand.class)
@CommandLine.Command(name = "create")
class GroupCreateCommand implements Runnable {

  @CommandLine.Mixin
  private final GroupTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final GroupManager manager;
  private final GroupCommandBeanMapper beanMapper;

  @NotBlank
  @CommandLine.Parameters(descriptionKey = "scm.group.create.name")
  private String name;

  @CommandLine.Option(names = {"--description", "-d"}, descriptionKey = "scm.group.create.desc")
  private String description;

  @CommandLine.Option(names = {"--member", "-m"}, descriptionKey = "scm.group.create.member")
  private String[] members;

  @CommandLine.Option(names = {"--external", "-e"}, descriptionKey = "scm.group.create.external")
  private boolean external;

  @Inject
  public GroupCreateCommand(GroupTemplateRenderer templateRenderer, CommandValidator validator, GroupManager manager, GroupCommandBeanMapper beanMapper) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.manager = manager;
    this.beanMapper = beanMapper;
  }

  @Override
  public void run() {
    validator.validate();
    Group newGroup = new Group("xml", name, members);
    newGroup.setDescription(description);
    newGroup.setExternal(external);
    Group createdGroup = manager.create(newGroup);
    templateRenderer.render(beanMapper.map(createdGroup));
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setDescription(String description) {
    this.description = description;
  }

  @VisibleForTesting
  void setMembers(String[] members) {
    this.members = members;
  }

  @VisibleForTesting
  void setExternal(boolean external) {
    this.external = external;
  }
}
