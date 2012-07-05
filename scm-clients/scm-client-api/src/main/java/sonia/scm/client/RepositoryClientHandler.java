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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tags;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public interface RepositoryClientHandler extends ClientHandler<Repository>
{

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   * @since 1.11
   */
  public Repository get(String type, String name);

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   * @since 1.8
   *
   * @throws NotSupportedFeatuerException
   */
  public ClientChangesetHandler getChangesetHandler(Repository repository)
    throws NotSupportedFeatuerException;

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   * @since 1.8
   *
   * @throws NotSupportedFeatuerException
   */
  public ClientRepositoryBrowser getRepositoryBrowser(Repository repository)
    throws NotSupportedFeatuerException;

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<Type> getRepositoryTypes();

  /**
   * Returns all tags of the given repository.
   *
   *
   * @param repository repository
   *
   * @return all tags of the given repository
   * @since 1.18
   */
  public Tags getTags(Repository repository);
}
