/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
