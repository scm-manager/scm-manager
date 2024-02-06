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


import sonia.scm.Handler;
import sonia.scm.FeatureNotSupportedException;
import sonia.scm.plugin.ExtensionPoint;

/**
 * Handler class for a specific {@link Repository} type.
 * These classes are singletons.
 *
 */
@ExtensionPoint
public interface RepositoryHandler
        extends Handler<Repository>
{


  /**
   * Returns the {@link ImportHandler} for the repository type of this handler.
   *
   *
   * @return {@link ImportHandler} for the repository type of this handler
   * @since 1.12
   * @deprecated
   *
   * @throws FeatureNotSupportedException
   */
  @Deprecated
  public ImportHandler getImportHandler() throws FeatureNotSupportedException;

  /**
   * Returns information about the version of the RepositoryHandler.
   * @since 1.15
   */
  public String getVersionInformation();

  @Override
  RepositoryType getType();
}
