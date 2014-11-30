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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import sonia.scm.repository.ImportResult;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.43
 */
public class ImportResultWrapper
{

  /**
   * Constructs ...
   *
   *
   * @param client
   * @param type
   * @param result
   */
  public ImportResultWrapper(RepositoryClientHandler client, String type,
    ImportResult result)
  {
    this.client = client;
    this.type = type;
    this.result = result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getFailedDirectories()
  {
    List<String> directories = result.getFailedDirectories();

    if (directories == null)
    {
      directories = ImmutableList.of();
    }

    return directories;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getImportedDirectories()
  {
    List<String> directories = result.getImportedDirectories();

    if (directories == null)
    {
      directories = ImmutableList.of();
    }

    return directories;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Repository> getImportedRepositories()
  {
    return Iterables.transform(getImportedDirectories(),
      new RepositoryResolver(client, type));
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/11/29
   * @author         Enter your name here...    
   */
  private static class RepositoryResolver
    implements Function<String, Repository>
  {

    /**
     * Constructs ...
     *
     *
     * @param clientHandler
     * @param type
     */
    public RepositoryResolver(RepositoryClientHandler clientHandler,
      String type)
    {
      this.clientHandler = clientHandler;
      this.type = type;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param name
     *
     * @return
     */
    @Override
    public Repository apply(String name)
    {
      return clientHandler.get(type, type);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final RepositoryClientHandler clientHandler;

    /** Field description */
    private final String type;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final RepositoryClientHandler client;

  /** Field description */
  private final ImportResult result;

  /** Field description */
  private final String type;
}
