/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
