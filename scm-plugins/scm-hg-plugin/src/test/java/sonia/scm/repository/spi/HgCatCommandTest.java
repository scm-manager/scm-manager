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

import org.junit.Ignore;
import org.junit.Test;
import sonia.scm.NotFoundException;
import sonia.scm.repository.InternalRepositoryException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class HgCatCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void testCat() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("a.txt");
    request.setRevision("a9bacaf1b7fa");
    assertEquals("a", execute(request));
  }

  @Test
  public void testSimpleCat() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("b.txt");
    assertEquals("b", execute(request));
  }

  @Test(expected = InternalRepositoryException.class)
  public void testUnknownFile() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("unknown");
    execute(request);
  }

  @Test(expected = NotFoundException.class)
  @Ignore("detection of unknown revision in hg not yet implemented")
  public void testUnknownRevision() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setRevision("abc");
    request.setPath("a.txt");
    execute(request);
  }

  @Test
  public void testSimpleStream() throws IOException {
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("b.txt");

    InputStream catResultStream = new HgCatCommand(cmdContext).getCatResultStream(request);

    assertEquals('b', catResultStream.read());
    assertEquals('\n', catResultStream.read());
    assertEquals(-1, catResultStream.read());

    catResultStream.close();
  }

  private String execute(CatCommandRequest request) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new HgCatCommand(cmdContext).getCatResult(request, baos);
    return baos.toString().trim();
  }
}
