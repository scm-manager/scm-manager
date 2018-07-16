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

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link RepositoryMatcher}.
 * 
 * @author Sebastian Sdorra
 * @since 1.54
 */
public class RepositoryMatcherTest {
  
  private RepositoryMatcher matcher;
  
  @Before
  public void setUp() {
    Set<RepositoryPathMatcher> pathMatchers = Sets.<RepositoryPathMatcher>newHashSet(new AbcRepositoryPathMatcher());
    this.matcher = new RepositoryMatcher(pathMatchers);
  }

  @Test
  public void testMatches() {
    assertFalse(matcher.matches(repository("hg", "scm"), "hg", "namespace/scm-test/ka"));
    assertFalse(matcher.matches(repository("git", "scm-test"), "hg", "namespace/scm-test"));
    
    assertTrue(matcher.matches(repository("hg", "scm-test"), "hg", "namespace/scm-test/ka"));
    assertTrue(matcher.matches(repository("hg", "scm-test"), "hg", "namespace/scm-test"));
  }
  
  @Test
  public void testMatchesWithCustomPathMatcher() {
    assertFalse(matcher.matches(repository("abc", "scm"), "hg", "/long/path/with/abc"));
    assertTrue(matcher.matches(repository("abc", "scm"), "abc", "/long/path/with/abc"));
  }
  
  private Repository repository(String type, String name) {
    return new Repository(type + "-" + name, type, "namespace", name);
  }
  
  private static class AbcRepositoryPathMatcher implements RepositoryPathMatcher {

    @Override
    public boolean isPathMatching(Repository repository, String path) {
      return path.endsWith("abc");
    }

    @Override
    public String getType() {
      return "abc";
    }
    
  }
  
}
