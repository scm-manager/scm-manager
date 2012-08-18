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

import com.google.common.io.Closeables;

import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class TemplateEngineTestBase
{

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  public abstract TemplateEngine createEngine(ServletContext context);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public abstract String getTemplateResource();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetTemlateNotFound() throws IOException
  {
    ServletContext context = mock(ServletContext.class);
    TemplateEngine engine = createEngine(context);
    Template template = engine.getTemplate("002");

    assertNull(template);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetTemplateFromClasspath() throws IOException
  {
    ServletContext context = mock(ServletContext.class);

    TemplateEngine engine = createEngine(context);

    assertNotNull(engine.getTemplate(getTemplateResource()));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetTemplateFromServletContext() throws IOException
  {

    String resource = getTemplateResource();

    if (!resource.startsWith("/"))
    {
      resource = "/".concat(resource);
    }

    URL url = MustacheTemplateEngineTest.class.getResource(resource);

    ServletContext context = mock(ServletContext.class);

    InputStream input = null;

    try
    {
      input = url.openStream();

      when(context.getResourceAsStream("001")).thenReturn(input);
      when(context.getResource("001")).thenReturn(url);
      when(context.getResourceAsStream("/001")).thenReturn(input);
      when(context.getResource("/001")).thenReturn(url);

      TemplateEngine engine = createEngine(context);

      assertNotNull(engine.getTemplate("001"));
    }
    finally
    {
      Closeables.closeQuietly(input);
    }
  }
}
