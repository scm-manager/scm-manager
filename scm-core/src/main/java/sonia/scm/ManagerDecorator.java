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


package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Comparator;

/**
 * Basic decorator for manager classes
 *
 * @author Sebastian Sdorra
 * @since 1.23
 *
 * @param <T>
 * @param <E>
 */
public class ManagerDecorator<T extends ModelObject, E extends Exception>
  implements Manager<T, E>
{

  /**
   * Constructs ...
   *
   *
   * @param decorated
   */
  public ManagerDecorator(Manager<T, E> decorated)
  {
    this.decorated = decorated;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    decorated.close();
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param object
   *
   * @throws E
   * @throws IOException
   */
  @Override
  public void create(T object) throws E, IOException
  {
    decorated.create(object);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param object
   *
   * @throws E
   * @throws IOException
   */
  @Override
  public void delete(T object) throws E, IOException
  {
    decorated.delete(object);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    decorated.init(context);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param object
   *
   * @throws E
   * @throws IOException
   */
  @Override
  public void modify(T object) throws E, IOException
  {
    decorated.modify(object);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param object
   *
   * @throws E
   * @throws IOException
   */
  @Override
  public void refresh(T object) throws E, IOException
  {
    decorated.refresh(object);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public T get(String id)
  {
    return decorated.get(id);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public Collection<T> getAll()
  {
    return decorated.getAll();
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param comparator
   *
   * @return
   */
  @Override
  public Collection<T> getAll(Comparator<T> comparator)
  {
    return decorated.getAll(comparator);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<T> getAll(int start, int limit)
  {
    return decorated.getAll(start, limit);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param comparator
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<T> getAll(Comparator<T> comparator, int start, int limit)
  {
    return decorated.getAll(comparator, start, limit);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public Long getLastModified()
  {
    return decorated.getLastModified();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Manager<T, E> decorated;
}
