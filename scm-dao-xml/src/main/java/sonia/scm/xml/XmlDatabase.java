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
    
package sonia.scm.xml;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public interface XmlDatabase<T>
{

  /**
   * Method description
   *
   *
   *
   * @param item
   */
  public void add(T item);

  /**
   * Method description
   *
   *
   *
   * @param id
   *
   * @return
   */
  public boolean contains(String id);

  /**
   * Method description
   *
   *
   *
   * @param id
   *
   * @return
   */
  public T remove(String id);

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<T> values();

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param id
   *
   * @return
   */
  public T get(String id);

  /**
   * Method description
   *
   *
   * @return
   */
  public long getCreationTime();

  /**
   * Method description
   *
   *
   * @return
   */
  public long getLastModified();

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param creationTime
   */
  public void setCreationTime(long creationTime);

  /**
   * Method description
   *
   *
   * @param lastModified
   */
  public void setLastModified(long lastModified);
}
