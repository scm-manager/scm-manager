/**
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


package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.ManagerDecorator;
import sonia.scm.search.SearchRequest;

import java.util.Collection;

//~--- JDK imports ------------------------------------------------------------

/**
 * Decorator for {@link UserManager}.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
public class UserManagerDecorator extends ManagerDecorator<User>
  implements UserManager
{

  /**
   * Constructs ...
   *
   *
   * @param decorated
   */
  public UserManagerDecorator(UserManager decorated)
  {
    super(decorated);
    this.decorated = decorated;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param username
   *
   * @return
   */
  @Override
  public boolean contains(String username)
  {
    return decorated.contains(username);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param searchRequest
   *
   * @return
   */
  @Override
  public Collection<User> search(SearchRequest searchRequest)
  {
    return decorated.search(searchRequest);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the decorated {@link UserManager}.
   *
   *
   * @return decorated {@link UserManager}
   * 
   * @since 1.34
   */
  public UserManager getDecorated()
  {
    return decorated;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String getDefaultType()
  {
    return decorated.getDefaultType();
  }

  @Override
  public void changePasswordForLoggedInUser(String oldPassword, String newPassword) {
    decorated.changePasswordForLoggedInUser(oldPassword, newPassword);
  }

  @Override
  public void overwritePassword(String userId, String newPassword) {
    decorated.overwritePassword(userId, newPassword);
  }
//~--- fields ---------------------------------------------------------------

  /** Field description */
  private final UserManager decorated;
}
