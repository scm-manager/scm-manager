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
    
package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

/**
 * Unit tests for {@link GitUserAgentProvider}.
 * 
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
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
