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
    
package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;

import sonia.scm.Undecorated;
import sonia.scm.util.Decorators;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class UserManagerProvider implements Provider<UserManager>
{

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public UserManager get()
  {
    return Decorators.decorate(userManagerProvider.get(), decoratorFactories);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param decoratorFactories
   */
  public void setDecoratorFactories(
    Set<UserManagerDecoratorFactory> decoratorFactories)
  {
    this.decoratorFactories = decoratorFactories;
  }

  /**
   * Method description
   *
   *
   * @param userManagerProvider
   */
  public void setUserManagerProvider(Provider<UserManager> userManagerProvider)
  {
    this.userManagerProvider = userManagerProvider;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject(optional = true)
  private Set<UserManagerDecoratorFactory> decoratorFactories;

  /** Field description */
  @Inject
  @Undecorated
  private Provider<UserManager> userManagerProvider;
}
