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

import sonia.scm.cli.wrapper.RepositoryWrapper;
import sonia.scm.client.ScmClientSession;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
@Command(
  name = "create-repository",
  usage = "usageCreateRepository",
  group = "repository"
)
public class CreateRepositorySubCommand extends TemplateSubCommand
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getContact()
  {
    return contact;
  }

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
  public boolean isPublicReadable()
  {
    return publicReadable;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param contact
   */
  public void setContact(String contact)
  {
    this.contact = contact;
  }

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
   * @param publicReadable
   */
  public void setPublicReadable(boolean publicReadable)
  {
    this.publicReadable = publicReadable;
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
    Repository repository = new Repository();

    repository.setName(name);
    repository.setType(type);
    repository.setContact(contact);
    repository.setDescription(description);

    ScmClientSession session = createSession();

    session.getRepositoryHandler().create(repository);

    Map<String, Object> env = new HashMap<String, Object>();

    env.put("repository", new RepositoryWrapper(repository));
    renderTemplate(env, GetRepositorySubCommand.TEMPLATE);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Option(
    name = "--contact",
    usage = "optionRepositoryContact",
    aliases = { "-c" }
  )
  private String contact;

  /** Field description */
  @Option(
    name = "--description",
    usage = "optionRepositoryDescription",
    aliases = { "-d" }
  )
  private String description;

  /** Field description */
  @Option(
    name = "--name",
    required = true,
    usage = "optionRepositoryName",
    aliases = { "-n" }
  )
  private String name;

  /** Field description */
  @Option(
    name = "--public",
    usage = "optionRepositoryPublic",
    aliases = { "-p" }
  )
  private boolean publicReadable;

  /** Field description */
  @Option(
    name = "--type",
    required = true,
    usage = "optionRepositoryType",
    aliases = { "-t" }
  )
  private String type;
}
