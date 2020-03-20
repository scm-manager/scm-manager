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
    
package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Binder;

/**
 * Process and resolve extensions.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public interface ExtensionProcessor
{

  /**
   * Collect extension classes by extension point.
   *
   *
   * @param <T> type of extension
   * @param extensionPoint extension point
   *
   * @return extensions
   */
  public <T> Iterable<Class<? extends T>> byExtensionPoint(
    Class<T> extensionPoint);

  /**
   * Returns single extension by its extension point.
   *
   *
   * @param <T> type of extension
   * @param extensionPoint extension point
   *
   * @return extension
   */
  public <T> Class<? extends T> oneByExtensionPoint(Class<T> extensionPoint);

  /**
   * Process auto bind extensions.
   *
   *
   * @param binder injection binder
   */
  public void processAutoBindExtensions(Binder binder);

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns all collected web elements (servlets and filters).
   *
   *
   * @return collected web elements
   */
  public Iterable<WebElementDescriptor> getWebElements();
}
