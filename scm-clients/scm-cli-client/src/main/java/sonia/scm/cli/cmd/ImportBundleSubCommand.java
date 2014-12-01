/**
* Copyright (c) 2014, Sebastian Sdorra All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* 1. Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer. 2. Redistributions in
* binary form must reproduce the above copyright notice, this list of
* conditions and the following disclaimer in the documentation and/or other
* materials provided with the distribution. 3. Neither the name of SCM-Manager;
* nor the names of its contributors may be used to endorse or promote products
* derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* http://bitbucket.org/sdorra/scm-manager
*
*/



package sonia.scm.cli.cmd;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Files;

import org.kohsuke.args4j.Option;

import sonia.scm.ConfigurationException;
import sonia.scm.client.ImportBundleRequest;
import sonia.scm.client.ScmClientSession;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.43
 */
@Command(
  name = "import-from-bundle",
  usage = "usageImportBundle",
  group = "repository"
)
public class ImportBundleSubCommand extends ImportSubCommand
{

  /**
   * Method description
   *
   *
   * @return
   */
  public File getBundle()
  {
    return bundle;
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param bundle
   */
  public void setBundle(File bundle)
  {
    this.bundle = bundle;
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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void run()
  {
    if (!bundle.exists())
    {
      throw new ConfigurationException("could not find bundle");
    }
    else
    {
      ScmClientSession session = createSession();

      Repository repository = session.getRepositoryHandler().importFromBundle(
                                new ImportBundleRequest(
                                  getType(), name, Files.asByteSource(bundle)));

      printImportedRepository(repository);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Option(
    name = "--bundle",
    required = true,
    usage = "optionRepositoryBundle",
    aliases = { "-b" }
  )
  private File bundle;

  /** Field description */
  @Option(
    name = "--name",
    required = true,
    usage = "optionRepositoryName",
    aliases = { "-n" }
  )
  private String name;
}
