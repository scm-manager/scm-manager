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
import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
@Command(
  name = "create-user",
  usage = "usageCreateUser",
  group = "user"
)
public class CreateUserSubCommand extends TemplateSubCommand
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getMail()
  {
    return mail;
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
  public String getPassword()
  {
    return password;
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

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isAdmin()
  {
    return admin;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param admin
   */
  public void setAdmin(boolean admin)
  {
    this.admin = admin;
  }

  /**
   * Method description
   *
   *
   * @param displayName
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  /**
   * Method description
   *
   *
   * @param mail
   */
  public void setMail(String mail)
  {
    this.mail = mail;
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
   * @param password
   */
  public void setPassword(String password)
  {
    this.password = password;
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
    User user = new User();

    user.setName(name);
    user.setAdmin(admin);
    user.setDisplayName(displayName);
    user.setPassword(password);
    user.setMail(mail);
    user.setType(type);

    ScmClientSession session = createSession();

    session.getUserHandler().create(user);

    Map<String, Object> env = new HashMap<String, Object>();

    env.put("user", user);
    renderTemplate(env, GetUserSubCommand.TEMPLATE);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Option(
    name = "--admin",
    usage = "optionUserAdmin",
    aliases = { "-a" }
  )
  private boolean admin = false;

  /** Field description */
  @Option(
    name = "--display-name",
    usage = "optionUserDisplayName",
    required = true,
    aliases = { "-d" }
  )
  private String displayName;

  /** Field description */
  @Option(
    name = "--mail",
    usage = "optionUserMail",
    aliases = { "-m" }
  )
  private String mail;

  /** Field description */
  @Option(
    name = "--name",
    usage = "optionUserName",
    required = true,
    aliases = { "-n" }
  )
  private String name;

  /** Field description */
  @Option(
    name = "--password",
    usage = "optionUserPassword",
    aliases = { "-p" }
  )
  private String password;

  /** Field description */
  @Option(
    name = "--type",
    usage = "optionUserType",
    aliases = { "-t" }
  )
  private String type = "xml";
}
