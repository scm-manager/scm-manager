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

package sonia.scm.web;


import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link GitUserAgentProvider}.
 * 
 * @author Sebastian Sdorra
 */
public class GitUserAgentProviderTest {

  private final GitUserAgentProvider provider = new GitUserAgentProvider();
  
  @Test
  public void testParseUserAgent() {
    assertEquals(GitUserAgentProvider.GIT, parse("git/1.7.9.5"));
    assertEquals(GitUserAgentProvider.JGIT, parse("jgit/4.5.2"));
    assertEquals(GitUserAgentProvider.GIT_LFS, parse("git-lfs/2.0.1 (GitHub; windows amd64; go 1.8; git 678cdbd4)"));
    assertEquals(GitUserAgentProvider.MSYSGIT, parse("git/1.8.3.msysgit.0"));
    assertEquals(GitUserAgentProvider.GOGIT, parse("go-git/5.x"));
    assertNull(parse("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"));
  }

  @Test
  public void testParseUserAgentCaseSensitive() {
    assertEquals(GitUserAgentProvider.GIT, parse("Git/1.7.9.5"));
  }
  
  @Test
  public void testParseUserAgentWithEmptyValue() {
    assertNull(parse(null));
  }
  
  @Test
  public void testParseUserAgentWithNullValue() {
    assertNull(parse(null));
  }

  private UserAgent parse(String v) {
    return provider.parseUserAgent(v);
  }
}
