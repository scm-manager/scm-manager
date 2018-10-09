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
 * Basic decorator for manager classes.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 *
 * @param <T> model type
 */
public class ManagerDecorator<T extends ModelObject> implements Manager<T> {

  /**
   * Constructs a new ManagerDecorator.
   *
   *
   * @param decorated manager implementation
   */
  public ManagerDecorator(Manager<T> decorated)
  {
    this.decorated = decorated;
  }

  @Override
  public void close() throws IOException
  {
    decorated.close();
  }

  @Override
  public T create(T object) throws AlreadyExistsException {
    return decorated.create(object);
  }

  @Override
  public void delete(T object) throws NotFoundException {
    decorated.delete(object);
  }

  @Override
  public void init(SCMContextProvider context)
  {
    decorated.init(context);
  }

  @Override
  public void modify(T object) throws NotFoundException {
    decorated.modify(object);
  }

  @Override
  public void refresh(T object) throws NotFoundException {
    decorated.refresh(object);
  }

  @Override
  public T get(String id)
  {
    return decorated.get(id);
  }

  @Override
  public Collection<T> getAll()
  {
    return decorated.getAll();
  }

  @Override
  public Collection<T> getAll(Comparator<T> comparator)
  {
    return decorated.getAll(comparator);
  }

  @Override
  public Collection<T> getAll(int start, int limit)
  {
    return decorated.getAll(start, limit);
  }

  @Override
  public Collection<T> getAll(Comparator<T> comparator, int start, int limit)
  {
    return decorated.getAll(comparator, start, limit);
  }

  @Override
  public Long getLastModified()
  {
    return decorated.getLastModified();
  }

  private Manager<T> decorated;
}
