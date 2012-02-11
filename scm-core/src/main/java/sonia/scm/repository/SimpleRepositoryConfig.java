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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Validateable;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Basic {@link Repository} configuration class.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement
public class SimpleRepositoryConfig implements Validateable
{

  /**
   * Returns the directory for the repositories.
   *
   *
   * @return directory for the repositories
   */
  public File getRepositoryDirectory()
  {
    return repositoryDirectory;
  }

  /**
   * Returns true if the plugin is disabled.
   *
   *
   * @return true if the plugin is disabled
   * @since 1.13
   */
  public boolean isDisabled()
  {
    return disabled;
  }

  /**
   * Returns true if the configuration object is valid.
   *
   *
   * @return true if the configuration object is valid
   */
  @Override
  public boolean isValid()
  {
    return repositoryDirectory != null;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Enable or disable the plugin.
   *
   *
   * @param disabled
   * @since 1.13
   */
  public void setDisabled(boolean disabled)
  {
    this.disabled = disabled;
  }

  /**
   * Sets the directory for the repositories
   *
   *
   * @param repositoryDirectory directory for repositories
   */
  public void setRepositoryDirectory(File repositoryDirectory)
  {
    this.repositoryDirectory = repositoryDirectory;
  }

  //~--- fields ---------------------------------------------------------------

  /** true if the plugin is disabled */
  private boolean disabled = false;

  /** directory for repositories */
  private File repositoryDirectory;
}
