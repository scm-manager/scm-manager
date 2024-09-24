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
