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


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitSubModuleParserTest
{

  /** Field description */
  public static final String GITMODULES_001 =
    "/sonia/scm/repository/gitmodules-001";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param resource
   *
   * @return
   *
   * @throws IOException
   */
  private String getContent(String resource) throws IOException
  {
    InputStream input =
      GitSubModuleParserTest.class.getResourceAsStream(resource);
    byte[] buffer = new byte[input.available()];

    input.read(buffer);

    return new String(buffer);
  }

  /**
   * Method description
   *
   *
   * @param resource
   *
   * @return
   *
   * @throws IOException
   */
  private Map<String, SubRepository> getSubRepositories(String resource)
          throws IOException
  {
    return GitSubModuleParser.parse(getContent(resource));
  }
}
