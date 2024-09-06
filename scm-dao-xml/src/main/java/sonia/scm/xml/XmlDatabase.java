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

package sonia.scm.xml;

import java.util.Collection;


public interface XmlDatabase<T>
{


  public void add(T item);


  public boolean contains(String id);


  public T remove(String id);


  public Collection<T> values();



  public T get(String id);


  public long getCreationTime();


  public long getLastModified();



  public void setCreationTime(long creationTime);


  public void setLastModified(long lastModified);
}
