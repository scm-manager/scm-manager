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

//~--- JDK imports ------------------------------------------------------------

import jakarta.servlet.ServletContext;

import java.net.URL;
import java.util.List;

/**
 * Load resources from {@link ServletContext} and from the installed plugins.
 * The UberWebResourceLoader will first look into the {@link ServletContext} and
 * afterwards it will search the plugin directories.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public interface UberWebResourceLoader extends WebResourceLoader
{

  /**
   * Returns all {@link URL} objects for the given path. The method will collect
   * all resources from {@link ServletContext} and all plugin directories which
   * matches the given path. The method will return an empty list, if no url
   * could be found for the given path.
   *
   * @param path resource path
   *
   * @return list of url objects for the given path
   */
  public List<URL> getResources(String path);
}
