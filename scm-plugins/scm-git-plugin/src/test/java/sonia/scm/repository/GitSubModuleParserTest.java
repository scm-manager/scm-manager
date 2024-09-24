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

package sonia.scm.repository;


import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;


public class GitSubModuleParserTest
{

  public static final String GITMODULES_001 =
    "/sonia/scm/repository/gitmodules-001";



  @Test
  public void testParse() throws IOException
  {
    Map<String, SubRepository> subRepositories =
      getSubRepositories(GITMODULES_001);
    SubRepository repository = subRepositories.get("gerrit");

    assertNotNull(repository);
    assertNotNull(repository.getRepositoryUrl());
    assertEquals("http://localhost:8081/scm/git/gerrit",
                 repository.getRepositoryUrl());
    repository = subRepositories.get("git-mod-1");
    assertNotNull(repository);
    assertNotNull(repository.getRepositoryUrl());
    assertEquals("http://localhost:8081/scm/git/sub/git-mod-1",
                 repository.getRepositoryUrl());
  }



  private String getContent(String resource) throws IOException
  {
    InputStream input =
      GitSubModuleParserTest.class.getResourceAsStream(resource);
    byte[] buffer = new byte[input.available()];

    input.read(buffer);

    return new String(buffer);
  }


  private Map<String, SubRepository> getSubRepositories(String resource)
          throws IOException
  {
    return GitSubModuleParser.parse(getContent(resource));
  }
}
