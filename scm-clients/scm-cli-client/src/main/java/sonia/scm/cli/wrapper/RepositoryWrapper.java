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



package sonia.scm.cli.wrapper;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.Permission;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryWrapper extends AbstractWrapper
{

  /**
   * Constructs ...
   *
   *
   * @param repository
   */
  public RepositoryWrapper(Repository repository)
  {
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getContact()
  {
    return repository.getContact();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Date getCreationDate()
  {
    return getDate(repository.getCreationDate());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return repository.getDescription();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return repository.getId();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Date getLastModified()
  {
    return getDate(repository.getLastModified());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return repository.getName();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<Permission> getPermissions()
  {
    return repository.getPermissions();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getType()
  {
    return repository.getType();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUrl()
  {
    return repository.getUrl();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPublicReadable()
  {
    return repository.isPublicReadable();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Repository repository;
}
