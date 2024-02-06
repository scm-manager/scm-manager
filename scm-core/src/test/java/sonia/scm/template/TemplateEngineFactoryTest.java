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


import com.google.common.collect.Sets;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class TemplateEngineFactoryTest
{

   @Test
  public void testGetDefaultEngine()
  {

    FakeTemplateEngine1 engine1 = new FakeTemplateEngine1();
    FakeTemplateEngine2 engine2 = new FakeTemplateEngine2();

    TemplateEngineFactory factory =
      new TemplateEngineFactory(Sets.newHashSet(engine1, engine2), engine2);

    assertSame(engine2, factory.getDefaultEngine());
  }

   @Test
  public void testGetEngine()
  {
    FakeTemplateEngine1 engine1 = new FakeTemplateEngine1();
    FakeTemplateEngine2 engine2 = new FakeTemplateEngine2();

    TemplateEngineFactory factory =
      new TemplateEngineFactory(Sets.newHashSet(engine1, engine2), engine2);

    assertSame(engine1, factory.getEngine("fake1"));
    assertSame(engine2, factory.getEngine("fake2"));
  }

   @Test
  public void testGetEngineByExtension()
  {
    FakeTemplateEngine1 engine1 = new FakeTemplateEngine1();
    FakeTemplateEngine2 engine2 = new FakeTemplateEngine2();

    TemplateEngineFactory factory =
      new TemplateEngineFactory(Sets.newHashSet(engine1, engine2), engine2);

    assertSame(engine1, factory.getEngineByExtension("fake1"));
    assertSame(engine2, factory.getEngineByExtension("fake2"));
    assertSame(engine1, factory.getEngineByExtension("fake"));
  }

   @Test
  public void testGetEngines()
  {
    FakeTemplateEngine1 engine1 = new FakeTemplateEngine1();
    FakeTemplateEngine2 engine2 = new FakeTemplateEngine2();

    TemplateEngineFactory factory =
      new TemplateEngineFactory(Sets.newHashSet(engine1, engine2), engine2);

    Collection<TemplateEngine> engines = factory.getEngines();

    assertEquals(2, engines.size());
    assertTrue(engines.contains(engine1));
    assertTrue(engines.contains(engine2));

    Set<TemplateEngine> ce = new HashSet<>();

    ce.add(engine1);
    factory = new TemplateEngineFactory(ce, engine2);

    engines = factory.getEngines();

    assertEquals(2, engines.size());
    assertTrue(engines.contains(engine1));
    assertTrue(engines.contains(engine2));
  }




  private static class FakeTemplateEngine1 implements TemplateEngine
  {

    /**
     * Method description
     *
     *
     * @param templatePath
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Template getTemplate(String templatePath) throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Method description
     *
     *
     * @param templateIdentifier
     * @param reader
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Template getTemplate(String templateIdentifier, Reader reader)
      throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

  
    @Override
    public TemplateType getType()
    {
      return new TemplateType("fake1", "Fake-Engine 1", "fake1", "fake");
    }
  }



  private static class FakeTemplateEngine2 implements TemplateEngine
  {

    /**
     * Method description
     *
     *
     * @param templateIdentifier
     * @param reader
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Template getTemplate(String templateIdentifier, Reader reader)
      throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Method description
     *
     *
     * @param templatePath
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Template getTemplate(String templatePath) throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

  
    @Override
    public TemplateType getType()
    {
      return new TemplateType("fake2", "Fake-Engine 2", "fake2");
    }
  }
}
