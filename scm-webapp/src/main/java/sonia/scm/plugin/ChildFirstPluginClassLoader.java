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

import sonia.scm.net.ChildFirstURLClassLoader;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

/**
 * Child first {@link ClassLoader} for SCM-Manager plugins.
 *
 * @author Sebastian Sdorra
 */
public class ChildFirstPluginClassLoader extends ChildFirstURLClassLoader
  implements PluginClassLoader
{

  private final String plugin;

  /**
   * Constructs ...
   *
   *
   * @param urls
   * @param parent
   */
  public ChildFirstPluginClassLoader(URL[] urls, ClassLoader parent, String plugin)
  {
    super(urls, parent);
    this.plugin = plugin;
  }

  @Override
  public String toString() {
    return ChildFirstPluginClassLoader.class.getName() + " for plugin " + plugin;
  }
}
