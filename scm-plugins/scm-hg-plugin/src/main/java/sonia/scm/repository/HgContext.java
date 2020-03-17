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


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class HgContext
{

  /**
   * Constructs ...
   *
   */
  public HgContext() {}

  /**
   * Constructs ...
   *
   *
   * @param pending
   */
  public HgContext(boolean pending)
  {
    this.pending = pending;
  }

  /**
   * Constructs ...
   *
   *
   * @param pending
   * @param systemEnvironment
   */
  public HgContext(boolean pending, boolean systemEnvironment)
  {
    this.pending = pending;
    this.systemEnvironment = systemEnvironment;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPending()
  {
    return pending;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSystemEnvironment()
  {
    return systemEnvironment;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param pending
   */
  public void setPending(boolean pending)
  {
    this.pending = pending;
  }

  /**
   * Method description
   *
   *
   * @param systemEnvironment
   */
  public void setSystemEnvironment(boolean systemEnvironment)
  {
    this.systemEnvironment = systemEnvironment;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean pending = false;

  /** Field description */
  private boolean systemEnvironment = true;
}
