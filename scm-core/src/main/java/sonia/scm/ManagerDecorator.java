/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
