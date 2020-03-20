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
    
package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Maps;

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.StringWriter;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class TemplateTestBase
{

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public abstract Template getFailureTemplate() throws IOException;

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public abstract Template getHelloTemplate() throws IOException;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testRender() throws IOException
  {
    Template template = getHelloTemplate();

    assertEquals("Hello marvin!", execute(template));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test(expected = IOException.class)
  public void testRenderFailure() throws IOException
  {
    Template template = getFailureTemplate();

    execute(template);
  }

  /**
   * Method description
   *
   *
   * @param env
   */
  protected void prepareEnv(Map<String, Object> env) {}

  /**
   * Method description
   *
   *
   * @param template
   *
   * @return
   *
   * @throws IOException
   */
  private String execute(Template template) throws IOException
  {
    Map<String, Object> env = Maps.newHashMap();

    env.put("name", "marvin");

    prepareEnv(env);

    StringWriter writer = new StringWriter();

    template.execute(writer, env);

    return writer.toString();
  }
}
