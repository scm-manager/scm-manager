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
