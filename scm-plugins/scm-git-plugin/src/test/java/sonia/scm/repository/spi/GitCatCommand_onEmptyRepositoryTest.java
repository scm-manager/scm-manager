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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sonia.scm.NotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GitCatCommand_onEmptyRepositoryTest extends AbstractGitCommandTestBase {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test(expected = NotFoundException.class)
  public void testCatResult() throws IOException {
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("a.txt");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new GitCatCommand(createContext(), null).getCatResult(request, baos);
  }

  @Test(expected = NotFoundException.class)
  public void testStream() throws IOException {
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("a.txt");
    new GitCatCommand(createContext(), null).getCatResultStream(request);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-empty-repo.zip";
  }
}
