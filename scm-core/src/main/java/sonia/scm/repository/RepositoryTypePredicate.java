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
    
package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;

/**
 * Used to filter collections of repositories by its type.
 *
 * @author Sebastian Sdorra
 * @since 1.16
 */
public class RepositoryTypePredicate implements Predicate<Repository>
{

  /**
   * Constructs new {@link RepositoryTypePredicate} object.
   *
   *
   * @param type type to filter
   */
  public RepositoryTypePredicate(String type)
  {
    this.type = type;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Return true if the repository is from the given type.
   *
   *
   * @param repository repository to check
   *
   * @return true if the repository is from the given type
   */
  @Override
  public boolean apply(Repository repository)
  {
    return type.equalsIgnoreCase(repository.getType());
  }

  //~--- fields ---------------------------------------------------------------

  /** type to filter */
  private String type;
}
