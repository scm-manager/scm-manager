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
