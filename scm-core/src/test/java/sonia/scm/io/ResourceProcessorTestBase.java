/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class ResourceProcessorTestBase
{

  /** Field description */
  public static final String TEXT_1 = "Hello Tricia McMillan!";

  /** Field description */
  public static final String TEXT_2 = "Hello ${person}!";

  /** Field description */
  public static final String TEXT_3 = "Hello ${person} and ${secondPerson}!";

  /** Field description */
  public static final String TEXT_4 = "<h1>Hello ${person}</h1>";

  /** Field description */
  public static final String VAR_PERSON = "person";

  /** Field description */
  public static final String VAR_PERSON_VALUE = "Tricia McMillan";

  /** Field description */
  public static final String VAR_SECONDPERSON = "secondPerson";

  /** Field description */
  public static final String VAR_SECONDPERSON_VALUE = "Zaphod Beeblebrox";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract ResourceProcessor createNewResourceProcessor();

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testHtml() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();

    processor.addVariable(VAR_PERSON, VAR_PERSON_VALUE);

    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_4), output);
    assertEquals("<h1>Hello Tricia McMillan</h1>", output.toString());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testWithNonExistingVar() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();
    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_2), output);
    assertEquals(TEXT_2, output.toString());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testWithVar() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();

    processor.addVariable(VAR_PERSON, VAR_PERSON_VALUE);

    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_2), output);
    assertEquals(TEXT_1, output.toString());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testWithVars() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();

    processor.addVariable(VAR_PERSON, VAR_PERSON_VALUE);
    processor.addVariable(VAR_SECONDPERSON, VAR_SECONDPERSON_VALUE);

    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_3), output);
    assertEquals("Hello Tricia McMillan and Zaphod Beeblebrox!",
                 output.toString());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testWithoutVars() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();
    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_1), output);
    assertEquals(TEXT_1, output.toString());
  }
}
