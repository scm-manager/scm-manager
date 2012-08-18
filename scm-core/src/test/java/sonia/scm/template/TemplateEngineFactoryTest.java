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

import com.google.common.collect.Sets;

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class TemplateEngineFactoryTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testGetDefaultEngine()
  {

    FakeTemplateEngine1 engine1 = new FakeTemplateEngine1();
    FakeTemplateEngine2 engine2 = new FakeTemplateEngine2();

    TemplateEngineFactory factory =
      new TemplateEngineFactory(Sets.newHashSet(engine1, engine2), engine2);

    assertSame(engine2, factory.getDefaultEngine());
  }

  /**
   * Method description
   *
   */
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

  /**
   * Method description
   *
   */
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

  /**
   * Method description
   *
   */
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

    Set<TemplateEngine> ce = new HashSet<TemplateEngine>();

    ce.add(engine1);
    factory = new TemplateEngineFactory(ce, engine2);

    engines = factory.getEngines();

    assertEquals(2, engines.size());
    assertTrue(engines.contains(engine1));
    assertTrue(engines.contains(engine2));
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/08/18
   * @author         Enter your name here...
   */
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
     * @return
     */
    @Override
    public TemplateType getType()
    {
      return new TemplateType("fake1", "Fake-Engine 1", "fake1", "fake");
    }
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/08/18
   * @author         Enter your name here...
   */
  private static class FakeTemplateEngine2 implements TemplateEngine
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
     * @return
     */
    @Override
    public TemplateType getType()
    {
      return new TemplateType("fake2", "Fake-Engine 2", "fake2");
    }
  }
}
