/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.cli.cmd;

//~--- non-JDK imports --------------------------------------------------------

import org.kohsuke.args4j.Option;

import sonia.scm.client.ScmClientSession;
import sonia.scm.group.Group;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
@Command(name="create-group", group="group")
public class CreateGroupSubCommand extends TemplateSubCommand
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getMembers()
  {
    return members;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getType()
  {
    return type;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param description
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Method description
   *
   *
   * @param members
   */
  public void setMembers(List<String> members)
  {
    this.members = members;
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Method description
   *
   *
   * @param type
   */
  public void setType(String type)
  {
    this.type = type;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void run()
  {
    Group group = new Group();

    group.setName(name);
    group.setDescription(description);
    group.setType(type);
    group.setMembers(members);

    ScmClientSession session = createSession();

    session.getGroupHandler().create(group);

    Map<String, Object> env = new HashMap<String, Object>();

    env.put("group", group);
    renderTemplate(env, GetGroupSubCommand.TEMPLATE);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Option(
    name = "--description",
    usage = "optionGroupDescription",
    aliases = { "-d" }
  )
  private String description;

  /** Field description */
  @Option(
    name = "--member",
    usage = "optionGroupMember",
    aliases = { "-m" }
  )
  private List<String> members;

  /** Field description */
  @Option(
    name = "--name",
    usage = "optionGroupName",
    required = true,
    aliases = { "-n" }
  )
  private String name;

  /** Field description */
  @Option(
    name = "--type",
    usage = "optionGroupType",
    aliases = { "-t" }
  )
  private String type = "xml";
}
