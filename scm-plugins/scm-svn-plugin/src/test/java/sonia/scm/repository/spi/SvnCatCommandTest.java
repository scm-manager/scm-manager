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

package sonia.scm.repository.spi;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sonia.scm.NotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class SvnCatCommandTest extends AbstractSvnCommandTestBase {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testCat() {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("a.txt");
    request.setRevision("1");
    assertEquals("a", execute(request));
  }

  @Test
  public void testSimpleCat() {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("c/d.txt");
    assertEquals("d", execute(request));
  }

  @Test
  public void testUnknownFile() {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("unknown");
    request.setRevision("1");

    expectedException.expect(new BaseMatcher<Object>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("expected NotFoundException for path");
      }

      @Override
      public boolean matches(Object item) {
        return "Path".equals(((NotFoundException)item).getContext().get(0).getType());
      }
    });

    execute(request);
  }

  @Test
  public void testUnknownRevision() {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("a.txt");
    request.setRevision("42");

    expectedException.expect(new BaseMatcher<Object>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("expected NotFoundException for revision");
      }

      @Override
      public boolean matches(Object item) {
        return "Revision".equals(((NotFoundException)item).getContext().get(0).getType());
      }
    });

    execute(request);
  }

  @Test
  public void testSimpleStream() throws IOException {
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("a.txt");
    request.setRevision("1");

    InputStream catResultStream = new SvnCatCommand(createContext()).getCatResultStream(request);

    assertEquals('a', catResultStream.read());
    assertEquals('\n', catResultStream.read());
    assertEquals(-1, catResultStream.read());

    catResultStream.close();
  }

  private String execute(CatCommandRequest request) {
    String content = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try
    {
      new SvnCatCommand(createContext()).getCatResult(request, baos);
    }
    finally
    {
      content = baos.toString().trim();
    }

    return content;
  }

  @Test
  public void shouldNotThrowExceptionForNonFileNodeCatCommand() throws IOException {
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("/");
    request.setRevision("1");

    InputStream catResultStream = new SvnCatCommand(createContext()).getCatResultStream(request);

    catResultStream.close();
  }
}
