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



package sonia.scm.repository.client;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.io.SimpleCommand;
import sonia.scm.io.SimpleCommandResult;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgRepositoryClient extends AbstractRepositoryClient
{

  /**
   * Constructs ...
   *
   *
   * @param localRepository
   * @param remoteRepository
   * @param username
   * @param password
   */
  HgRepositoryClient(File localRepository, String remoteRepository,
                     String username, String password)
  {
    super(localRepository, remoteRepository);

    String scheme = remoteRepository.substring(0,
                      remoteRepository.indexOf("://") + 3);
    StringBuilder buffer = new StringBuilder(scheme);

    buffer.append(username).append(":").append(password).append("@");
    buffer.append(remoteRepository.substring(scheme.length()));
    remoteURL = buffer.toString();
    hg = IOUtil.search("hg");
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   * @param others
   *
   * @throws RepositoryClientException
   */
  @Override
  public void add(String file, String... others)
          throws RepositoryClientException
  {
    addFile(file);

    if (others != null)
    {
      for (String o : others)
      {
        addFile(o);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @throws RepositoryClientException
   */
  @Override
  public void checkout() throws RepositoryClientException
  {
    if (!isInitialized())
    {
      init();
    }

    SimpleCommand cmd = new SimpleCommand(hg, "-R",
                          localRepository.getAbsolutePath(), "pull", "-u",
                          remoteURL);

    execute(cmd);
  }

  /**
   * Method description
   *
   *
   * @param message
   *
   * @throws RepositoryClientException
   */
  @Override
  public void commit(String message) throws RepositoryClientException
  {
    SimpleCommand cmd = new SimpleCommand(hg, "-R",
                          localRepository.getAbsolutePath(), "commit", "-m",
                          message);

    execute(cmd);
    cmd = new SimpleCommand(hg, "-R", localRepository.getAbsolutePath(),
                            "push", remoteURL);
    execute(cmd);
  }

  /**
   * Method description
   *
   *
   * @throws RepositoryClientException
   */
  @Override
  public void init() throws RepositoryClientException
  {
    SimpleCommand cmd = new SimpleCommand(hg, "init",
                          localRepository.getAbsolutePath());

    execute(cmd);
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @throws RepositoryClientException
   */
  private void addFile(String file) throws RepositoryClientException
  {
    SimpleCommand cmd = new SimpleCommand(hg, "-R",
                          localRepository.getAbsolutePath(), "add",
                          new File(localRepository, file).getAbsolutePath());

    execute(cmd);
  }

  /**
   * Method description
   *
   *
   * @param cmd
   *
   * @throws RepositoryClientException
   */
  private void execute(SimpleCommand cmd) throws RepositoryClientException
  {
    try
    {
      SimpleCommandResult result = cmd.execute();

      if (!result.isSuccessfull())
      {
        throw new RepositoryClientException(result.getOutput());
      }
    }
    catch (IOException ex)
    {
      throw new RepositoryClientException(ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private boolean isInitialized()
  {
    return new File(localRepository, ".hg").exists();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String hg;

  /** Field description */
  private String remoteURL;
}
