/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Basic decorator for manager classes.
 *
 * @since 1.23
 *
 * @param <T> model type
 */
public class ManagerDecorator<T extends ModelObject> implements Manager<T> {

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
  public T create(T object) {
    return decorated.create(object);
  }

  @Override
  public void delete(T object){
    decorated.delete(object);
  }

  @Override
  public void init(SCMContextProvider context)
  {
    decorated.init(context);
  }

  @Override
  public void modify(T object){
    decorated.modify(object);
  }

  @Override
  public void refresh(T object){
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
  public Collection<T> getAll(Predicate<T> filter, Comparator<T> comparator)
  {
    return decorated.getAll(filter, comparator);
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
