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

/**
 * The constants in this class represent the current state of the running
 * SCM_Manager instance. The stage can be queried by calling
 * {@link SCMContextProvider#getStage()}.
 *
 * @author Sebastian Sdorra
 * @since 1.12
 */
public enum Stage
{

  /**
   * This value indicates SCM-Manager is right now in development.
   */
  DEVELOPMENT(com.google.inject.Stage.DEVELOPMENT),

  /**
   * This value indicates SCM-Manager is right now productive.
   */
  PRODUCTION(com.google.inject.Stage.PRODUCTION),

  /**
   * This value indicates SCM-Manager is right now in development but specifically configured for testing.
   * @since 2.47.0
   */
  TESTING(com.google.inject.Stage.DEVELOPMENT);

  /**
   * Constructs a new Stage
   *
   *
   * @param injectionStage injection stage
   */
  private Stage(com.google.inject.Stage injectionStage)
  {
    this.injectionStage = injectionStage;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the injection stage
   *
   *
   * @return injection stage
   * 
   * @since 2.0.0
   */
  public com.google.inject.Stage getInjectionStage()
  {
    return injectionStage;
  }

  //~--- fields ---------------------------------------------------------------

  /** injection stage */
  private final com.google.inject.Stage injectionStage;
}
