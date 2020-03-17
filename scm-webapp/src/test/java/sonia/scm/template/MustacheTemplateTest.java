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

package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import com.google.common.base.Function;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class MustacheTemplateTest extends TemplateTestBase
{

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public Template getFailureTemplate() throws IOException
  {
    return getTemplate("sonia/scm/template/003.mustache");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Template getHelloTemplate()
  {
    return getTemplate("sonia/scm/template/001.mustache");
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param env
   */
  @Override
  protected void prepareEnv(Map<String, Object> env)
  {
    env.put("test", (Function<String, String>) input -> {
      throw new UnsupportedOperationException("Not supported yet.");
    });
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private Template getTemplate(String path)
  {
    DefaultMustacheFactory factory = new DefaultMustacheFactory();
    Mustache mustache = factory.compile(path);

    return new MustacheTemplate(path, mustache);
  }
}
