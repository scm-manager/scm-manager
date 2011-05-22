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

import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
@Command(
  name = "add-permission",
  usage = "usageAddPermission",
  group = "repository"
)
public class AddPermissionSubCommand extends PermissionSubCommand
{

  /**
   * Method description
   *
   *
   * @return
   */
  public PermissionType getType()
  {
    return type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isGroup()
  {
    return group;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param group
   */
  public void setGroup(boolean group)
  {
    this.group = group;
  }

  /**
   * Method description
   *
   *
   * @param type
   */
  public void setType(PermissionType type)
  {
    this.type = type;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permissions
   */
  @Override
  protected void modifyPermissions(List<Permission> permissions)
  {
    permissions.add(new Permission(name, type, group));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Option(
    name = "--group",
    usage = "optionPermissionGroup",
    aliases = { "-g" }
  )
  private boolean group = false;

  /** Field description */
  @Option(
    name = "--name",
    usage = "optionPermissionName",
    required = true,
    aliases = { "-n" }
  )
  private String name;

  /** Field description */
  @Option(
    name = "--type",
    usage = "optionPermissionType",
    required = true,
    metaVar="metaVar_permissionType",
    aliases = { "-t" }
  )
  private PermissionType type;
}
