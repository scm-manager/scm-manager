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
    
package sonia.scm.io;


import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


public abstract class ResourceProcessorTestBase
{

  public static final String TEXT_1 = "Hello Tricia McMillan!";

  public static final String TEXT_2 = "Hello ${person}!";

  public static final String TEXT_3 = "Hello ${person} and ${secondPerson}!";

  public static final String TEXT_4 = "<h1>Hello ${person}</h1>";

  public static final String VAR_PERSON = "person";

  public static final String VAR_PERSON_VALUE = "Tricia McMillan";

  public static final String VAR_SECONDPERSON = "secondPerson";

  public static final String VAR_SECONDPERSON_VALUE = "Zaphod Beeblebrox";


  
  protected abstract ResourceProcessor createNewResourceProcessor();


  @Test
  public void testHtml() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();

    processor.addVariable(VAR_PERSON, VAR_PERSON_VALUE);

    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_4), output);
    assertEquals("<h1>Hello Tricia McMillan</h1>", output.toString());
  }


  @Test
  public void testWithNonExistingVar() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();
    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_2), output);
    assertEquals(TEXT_2, output.toString());
  }


  @Test
  public void testWithVar() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();

    processor.addVariable(VAR_PERSON, VAR_PERSON_VALUE);

    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_2), output);
    assertEquals(TEXT_1, output.toString());
  }


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


  @Test
  public void testWithoutVars() throws IOException
  {
    ResourceProcessor processor = createNewResourceProcessor();
    StringWriter output = new StringWriter();

    processor.process(new StringReader(TEXT_1), output);
    assertEquals(TEXT_1, output.toString());
  }
}
