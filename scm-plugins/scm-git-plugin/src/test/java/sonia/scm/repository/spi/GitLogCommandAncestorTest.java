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

import org.junit.Test;
import sonia.scm.NotFoundException;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitTestHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link GitLogCommand} with an ancestor commit. This test uses the following git repository:
 *
 * <pre>
 * * 86e9ca0 (HEAD -> b) b5
 * *   d69edb3 Merge branch 'master' into b
 * |\
 * | * 946a8db (master) f
 * | * b19b9cc e
 * * | 3d6109c b4
 * * | 6330653 b3
 * * |   a49a28e Merge branch 'master' into b
 * |\ \
 * | |/
 * | * 0235584 d
 * | * 20251c5 c
 * * | 5023b85 b2
 * * | 201ecc1 b1
 * |/
 * * 36b19e4 b
 * * c2190a9 a
 * </pre>
 */
public class GitLogCommandAncestorTest extends AbstractGitCommandTestBase
{
  @Override
  protected String getZippedRepositoryResource()
  {
    return "sonia/scm/repository/spi/scm-git-ancestor-test.zip";
  }

  @Test
  public void testGetAncestor()
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setBranch("b");
    request.setAncestorChangeset("master");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(7, result.getTotal());
    assertEquals(7, result.getChangesets().size());

    assertEquals("86e9ca012202b36865373a63c12ef4f4353506cd", result.getChangesets().get(0).getId());
    assertEquals("d69edb314d07ab20ad626e3101597702d3510b5d", result.getChangesets().get(1).getId());
    assertEquals("3d6109c4c830e91eaf12ac6a331a5fccd670fe3c", result.getChangesets().get(2).getId());
    assertEquals("63306538d06924d6b254f86541c638021c001141", result.getChangesets().get(3).getId());
    assertEquals("a49a28e0beb0ab55f985598d05b8628c2231c9b6", result.getChangesets().get(4).getId());
    assertEquals("5023b850c2077db857593a3c0269329c254a370d", result.getChangesets().get(5).getId());
    assertEquals("201ecc1131e6b99fb0a0fe9dcbc8c044383e1a07", result.getChangesets().get(6).getId());
  }

  @Test(expected = NotFoundException.class)
  public void testAncestorWithDeletedSourceBranch()
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setBranch("no_such_branch");
    request.setAncestorChangeset("master");

    createCommand().getChangesets(request);
  }

  @Test(expected = NotFoundException.class)
  public void testAncestorWithDeletedAncestorBranch()
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setBranch("b");
    request.setAncestorChangeset("no_such_branch");

    createCommand().getChangesets(request);
  }

  private GitLogCommand createCommand() {
    return new GitLogCommand(createContext(), GitTestHelper.createConverterFactory());
  }
}
