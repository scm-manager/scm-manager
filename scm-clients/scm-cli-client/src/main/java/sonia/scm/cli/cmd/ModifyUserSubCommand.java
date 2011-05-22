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

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import sonia.scm.cli.I18n;
import sonia.scm.client.ScmClientSession;
import sonia.scm.client.UserClientHandler;
import sonia.scm.user.User;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
@Command(
  name = "modify-user",
  usage = "usageModifyUser",
  group = "user"
)
public class ModifyUserSubCommand extends TemplateSubCommand
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

  //~--- set methods ----------------------------------------------------------

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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void run()
  {
    ScmClientSession session = createSession();
    UserClientHandler handler = session.getUserHandler();
    User user = handler.get(name);

    if (user != null)
    {
      if (Util.isNotEmpty(displayName))
      {
        user.setDisplayName(displayName);
      }

      if (Util.isNotEmpty(mail))
      {
        user.setMail(mail);
      }

      if (Util.isNotEmpty(password))
      {
        user.setPassword(password);
      }

      handler.modify(user);

      Map<String, Object> env = new HashMap<String, Object>();

      env.put("user", user);
      renderTemplate(env, GetUserSubCommand.TEMPLATE);
    }
    else
    {
      output.println(i18n.getMessage(I18n.USER_NOT_FOUND));
    }
  }

  //~--- fields ---------------------------------------------------------------

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
  @Argument(usage = "optionUserName", required = true)
  private String name;

  /** Field description */
  @Option(
    name = "--password",
    usage = "optionUserPassword",
    aliases = { "-p" }
  )
  private String password;
}
