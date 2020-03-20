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

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import java.net.URL;

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import sonia.scm.util.IOUtil;

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
  public abstract String getDefectTemplateResource();

  /**
   * Method description
   *
   *
   * @return
   */
  public abstract String getTemplateResource();

  public abstract String getTemplateResourceWithGermanTranslation();

  /**
   * Method description
   *
   *
   * @param resource
   *
   * @return
   */
  protected abstract InputStream getResource(String resource);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test(expected = IOException.class)
  public void testGetDefectTemplate() throws IOException
  {
    ServletContext context = mock(ServletContext.class);
    TemplateEngine engine = createEngine(context);

    engine.getTemplate(getDefectTemplateResource());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetTemplateNotFound() throws IOException
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

  @Test
  public void testGetTemplateFromClasspathWithExistingLocale() throws IOException
  {
    ServletContext context = mock(ServletContext.class);

    TemplateEngine engine = createEngine(context);

    Template template = engine.getTemplate(getTemplateResourceWithGermanTranslation(), Locale.GERMAN);
    StringWriter writer = new StringWriter();
    template.execute(writer, new Object());
    Assertions.assertThat(writer.toString()).contains("German");
  }

  @Test
  public void testGetTemplateFromClasspathWithNotExistingLocale() throws IOException
  {
    ServletContext context = mock(ServletContext.class);

    TemplateEngine engine = createEngine(context);

    Template template = engine.getTemplate(getTemplateResourceWithGermanTranslation(), Locale.CHINESE);
    StringWriter writer = new StringWriter();
    template.execute(writer, new Object());
    Assertions.assertThat(writer.toString()).contains("English");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetTemplateFromReader() throws IOException
  {
    ServletContext context = mock(ServletContext.class);
    TemplateEngine engine = createEngine(context);

    InputStream input = null;

    try
    {
      input = getResource("007");

      Template template = engine.getTemplate("007",
                            new InputStreamReader(input));

      assertNotNull(template);

      Map<String, Object> env = Maps.newHashMap();

      env.put("time", "Lunchtime");

      StringWriter writer = new StringWriter();

      template.execute(writer, env);
      assertEquals("Time is an illusion. Lunchtime doubly so.",
        writer.toString());
    }
    finally
    {
      IOUtil.close(input);
    }
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
      IOUtil.close(input);
    }
  }
}
